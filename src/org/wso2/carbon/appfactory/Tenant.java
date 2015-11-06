package org.wso2.carbon.appfactory;

/**
 * Created by punnadi on 8/13/15.
 */
public class Tenant {

    private String tenantDomain;

    private int tenantId;

    private String email;

    private String admin;

    public Tenant(int tenantId, String tenantDomain, String email, String tenantAdmin) {
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
        this.email = email;
        this.admin =  tenantAdmin;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getEmail() {
        return email;
    }

    public String getAdmin() {
        return admin;
    }

    @Override
    public String toString() {
        return "Tenant Id: " + tenantId + " Tenant Domain: " + tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(String admin){
        this.admin = admin;
    }
}
