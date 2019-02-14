package io.github.formular_team.formular;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

public final class Formular extends CordovaPlugin {
    @Override
    public void pluginInitialize() {
        super.pluginInitialize();
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        /*
         * user_properties/set
         * user_properties/get
         * course_library/get
         * gesture/select (x, y)
         * find_tournament/start
         * find_tournament/stop
         * tournament/start (tournament_properties)
         * tournament/stop
         */
        return super.execute(action, args, callbackContext);
    }
}
