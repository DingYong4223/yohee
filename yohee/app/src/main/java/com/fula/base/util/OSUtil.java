package com.fula.base.util;

import android.text.TextUtils;

import java.lang.reflect.Method;

public class OSUtil {

    //MIUI标识
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    //EMUI标识
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    //Flyme标识
    private static final String KEY_FLYME_ID_FALG_KEY = "ro.build.display.id";
    private static final String KEY_FLYME_ID_FALG_VALUE_KEYWORD = "Flyme";
    private static final String KEY_FLYME_ICON_FALG = "persist.sys.use.flyme.icon";
    private static final String KEY_FLYME_SETUP_FALG = "ro.meizu.setupwizard.flyme";
    private static final String KEY_FLYME_PUBLISH_FALG = "ro.flyme.published";

    /**
     * 是否是Flyme系统
     *
     * @return
     */
    public static boolean isFlyme() {
        if (isPropertiesExist(KEY_FLYME_ICON_FALG, KEY_FLYME_SETUP_FALG, KEY_FLYME_PUBLISH_FALG)) {
            return true;
        }
        String romName = getSystemProperty(KEY_FLYME_ID_FALG_KEY);
        if (!TextUtils.isEmpty(romName) && romName.contains(KEY_FLYME_ID_FALG_VALUE_KEYWORD)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是EMUI系统
     *
     * @return
     */
    public static boolean isEMUI() {
        return isPropertiesExist(KEY_EMUI_VERSION_CODE, KEY_EMUI_API_LEVEL,
                KEY_EMUI_CONFIG_HW_SYS_VERSION);
    }

    /**
     * 是否是MIUI系统
     *
     * @return
     */
    public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_CODE))
                || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_NAME))
                || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_INTERNAL_STORAGE));
    }

    /**
     * Returns a SystemProperty
     *
     * @param propName The Property to retrieve
     * @return The Property, or NULL if not found
     */
    public static String getSystemProperty(String propName) {
        String value = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, propName, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private static boolean isPropertiesExist(String... keys) {
        if (keys == null || keys.length == 0) {
            return false;
        }
        for (String key : keys) {
            String value = getSystemProperty(key);
            if (value != null)
                return true;
        }
        return false;
    }

}
