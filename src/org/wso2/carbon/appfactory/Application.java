package org.wso2.carbon.appfactory;

/**
 * Created by punnadi on 8/21/15.
 */
public class Application {
    private String stage;
    private String application_id;
    private String cartridge_type;

    public Application(String stage, String application_id, String cartridge_type) {
        this.stage = stage;
        this.application_id = application_id;
        this.cartridge_type = cartridge_type;
    }

    public String getStage() {
        return stage;
    }

    public String getApplication_id() {
        return application_id;
    }

    public String getCartridge_type() {
        return cartridge_type;
    }
}
