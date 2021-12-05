package com.ping.classloader;


import lombok.extern.slf4j.Slf4j;
import sun.misc.Launcher;
import sun.misc.URLClassPath;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @Author: W.Z
 * @Date: 2021/11/28 16:56
 */
@Slf4j
public class JvmClassLoaderPrintPath {

    public static void main(String[] args) {
        URLClassPath classPath = Launcher.getBootstrapClassPath();
        log.info("启动类加载器");
        URL[] urLs = classPath.getURLs();
        for (URL urL : urLs) {
            log.info("-->{}", urL.toExternalForm());
        }

        printClassLoader("扩展类加载器", JvmClassLoaderPrintPath.class.getClassLoader().getParent());

        printClassLoader("应用类加载器", JvmClassLoaderPrintPath.class.getClassLoader());
    }


    public static void printClassLoader(String name, ClassLoader classLoader) {
        if (classLoader == null) {
            log.info("{} ClassLoader  is null ", name);
        } else {
            log.info("{} ClassLoader  {} ", name, classLoader.toString());
            printUrlFromClassLoader(classLoader);
        }
    }

    private static void printUrlFromClassLoader(ClassLoader classLoader) {

        Object ucp = insightField(classLoader, "ucp");
        Object path = insightField(ucp, "path");

        ArrayList list = (ArrayList) path;
        for (Object o : list) {
            log.info("--> {}", o);
        }
    }

    private static Object insightField(Object obj, String name) {
        try {
            Field field = null;
            if (obj instanceof URLClassLoader) {
                field = URLClassLoader.class.getDeclaredField(name);
            } else {
                field = obj.getClass().getDeclaredField(name);
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
