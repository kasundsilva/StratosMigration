package org.wso2.carbon.appfactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);
    private static final PropertyLoader prop = PropertyLoader.getInstance();

    private static Connection connection = null;

    private static void createConnection(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(prop.getProperty("datasource"));
    }

    private static void closeConnection() throws SQLException {
        connection.close();
    }

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", prop.getProperty("trust_store_path"));
        System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("trust_store_password"));
        System.setProperty("javax.net.ssl.trustStore", prop.getProperty("trust_store_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("trust_store_password"));

        try {
            createConnection(args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Tenant> tenants = null;
        try {
            tenants = getTenantIDs();
            log.info("# of Tenants available : " + tenants.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Application> applications = null;
        if (tenants.size() > 0) {
            applications = getApplications();
            log.info("# of Applications available : " + applications.size());
        }

        int limit = Integer.parseInt(prop.getProperty("limit"));
        int counter = 0;
        for (Tenant tenant : tenants) {
            counter ++;
            log.info("Tenant : " + tenant.getTenantDomain());
            try {
                //createTenant(tenant);
                for (Application application : applications) {

                    // Adding  signup
                    log.info(" Signing up tenant : " + tenant.getTenantDomain() + " Tenant id : " + tenant.getTenantId() + " for application : " + application.getCartridge_type());
                    signUp(tenant, application);

                    // Remove signup
                    //log.info("deleting applications : " + getEndPointUrl("/applications/" + application.getApplication_id() + "/signup"));
                    //deleteSignUp(tenant, application);
                    Thread.sleep(Long.parseLong(prop.getProperty("sleep_time")));
                }
                if (counter >= limit){
                    break;
                }
            } catch (Exception e) {
                String msg = "error occurred while application signup for tenant " + tenant;
                log.error(msg, e);
            }
        }

    }

    private static List<Application> getApplications() {
        List<Application> applications = new ArrayList<Application>();
        String data = prop.getProperty("stage_param");
        final String[] envs = data.split(":");
        for (String env : envs) {
            final String[] vals = env.split(",");
            applications.add(new Application(vals[0].trim(), vals[1].trim(), vals[2].trim()));
        }
        return applications;
    }

    public static void createTenant(Tenant tenant) throws Exception {
        try {
            StratosHttpClient.sendPostRequest(generateAddTenantBody(tenant), getEndPointUrl("/tenants"),
                                              prop.getProperty("super_tenant_username"),
                                              prop.getProperty("super_tenant_password"));
        } catch (Exception e) {
            String msg = "error occurred while creating the tenant " + tenant;
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    private static String getEndPointUrl(String uri) {
        return prop.getProperty("stratos_url") + "/api" + uri;
    }

    public static void signUp(Tenant tenant, Application application) throws Exception {
        try {
            log.info("##################### SignUp body : " + generateSignUpBody(application.getApplication_id(),
                    getGitRepoURL(application.getStage(), tenant.getTenantId(), prop.getProperty("runtime"))));
            log.info("##################### EndPoint Url : " + getEndPointUrl("/applications/" + application.getApplication_id() + "/signup"));
            log.info("##################### Authenticating as Tenant : " + tenant.getAdmin() + "@" + tenant.getTenantDomain());

            StratosHttpClient.sendPostRequest(
                    generateSignUpBody(application.getApplication_id(),
                                       getGitRepoURL(application.getStage(), tenant.getTenantId(),
                                               prop.getProperty("runtime"))),
                    getEndPointUrl("/applications/" + application.getApplication_id() + "/signup"),
                    tenant.getAdmin() + "@" + tenant.getTenantDomain(), "garbage");
        } catch (Exception e) {
            String msg =
                    "error occurred while creating the tenant " + tenant + " in environment " + application.getStage();
            log.error(msg, e);
            log.info("");
            throw new Exception(msg, e);
        }
    }

    public static void deleteSignUp(Tenant tenant, Application application) throws Exception {
        try {
            StratosHttpClient.sendDeleteRequest(
                    getEndPointUrl("/applications/" + application.getApplication_id() + "/signup"),
                    tenant.getAdmin() + "@" + tenant.getTenantDomain(), "garbage");

        } catch (Exception e) {
            String msg =
                    "error occurred while creating the tenant " + tenant + " in environment " + application.getStage();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
    private static List<Tenant> getTenantIDs() throws Exception {
        List<Tenant> tenants = new ArrayList<Tenant>();
        String sql = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, CAST(UM_USER_CONFIG AS CHAR(10000) CHARACTER SET utf8) AS CONTENT FROM UM_TENANT";
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("UM_ID");
                String tenantDomain = resultSet.getString("UM_DOMAIN_NAME");
                String email = resultSet.getString("UM_EMAIL");
                String content = resultSet.getString("CONTENT");
                String tenantAdmin = (content.split("</UserName>")[0]).split("<UserName>")[1];
                tenants.add(new Tenant(id, tenantDomain, email, tenantAdmin));
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving tenant information", e);
            throw new Exception("Error occurred while retrieving tenant ids", e);
        } finally {
            resultSet.close();
            preparedStatement.close();
        }
        return tenants;
    }

    private static String getGitRepoURL(String stage, int tenantId, final String runtime) {
        return prop.getProperty("s2git_url") + "/r/" + stage + "/" + runtime + "/" + tenantId + ".git";
    }

    public static String generateAddTenantBody(Tenant tenant) throws Exception {
        JSONObject tenantObj = new JSONObject();
        try {
            tenantObj.put("admin", tenant.getAdmin());
            tenantObj.put("firstName", "");
            tenantObj.put("lastName", "");
            tenantObj.put("adminPassword", "");
            tenantObj.put("tenantDomain", tenant.getTenantDomain());
            tenantObj.put("email", tenant.getEmail());
            tenantObj.put("active", true);

        } catch (JSONException e) {
            String errorMsg = "Error while generating json string for add tenant";
            log.error(errorMsg, e);
            throw new Exception(errorMsg);
        }
        return tenantObj.toString();
    }

    public static String generateSignUpBody(String applicationAlias, String repoUrl) throws Exception {
        JSONObject repo = new JSONObject();
        JSONObject repos = new JSONObject();
        try {
            repo.put("alias", applicationAlias);
            repo.put("privateRepo", false);
            repo.put("repoUrl", repoUrl);
            repo.put("repoUsername", "s2gituser");
            repo.put("repoPassword", "s2gituser");
            repos.put("artifactRepositories", repo);
        } catch (JSONException e) {
            String errorMsg = "Error while generating json string for signup";
            log.error(errorMsg, e);
            throw new Exception(errorMsg);
        }
        return repos.toString();
    }
}
