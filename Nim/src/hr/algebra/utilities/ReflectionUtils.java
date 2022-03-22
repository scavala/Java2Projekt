/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utilities;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sime
 */
public class ReflectionUtils {

    private ReflectionUtils() {
    }

    private static void documentClass(String file, StringBuilder htmlBuilder, String packageName) {
        if (file.endsWith(".java")) {

            htmlBuilder.append("<h2>Paket:");
            htmlBuilder.append(packageName);
            htmlBuilder.append("</h2>");
            htmlBuilder.append(System.lineSeparator());

            htmlBuilder.append("<h3>");
            htmlBuilder.append(file);
            htmlBuilder.append("</h3>");
            htmlBuilder.append(System.lineSeparator());

            try {
                Class c = Class.forName(
                        packageName + "."
                        + file.substring(0, file.indexOf(".")));
                appendParent(c, htmlBuilder, true);
                appendInterfaces(c, htmlBuilder);
                Field[] fields = c.getDeclaredFields();

                if (fields.length > 0) {
                    htmlBuilder.append("<br/><b>Varijable:</b><br/>");
                }

                for (Field field : fields) {
                    int modifiers = field.getModifiers();

                    extractModifiers(modifiers, htmlBuilder);

                    htmlBuilder.append(field.getType().getName());
                    htmlBuilder.append(" ");
                    htmlBuilder.append(field.getName());
                    htmlBuilder.append("<br />");
                }

                Constructor[] constructors = c.getDeclaredConstructors();

                htmlBuilder.append("<br /><b>Konstruktori:</b><br />");

                for (Constructor constructor : constructors) {
                    int modifiers = constructor.getModifiers();

                    extractModifiers(modifiers, htmlBuilder);

                    htmlBuilder.append(constructor.getName());
                    htmlBuilder.append("(");

                    Parameter[] params = constructor.getParameters();

                    for (Parameter param : params) {
                        int paramModifiers = param.getModifiers();

                        extractModifiers(paramModifiers, htmlBuilder);

                        htmlBuilder.append(param.getType().getName());
                        htmlBuilder.append(" ");
                        htmlBuilder.append(param.getName());
                        if (params[params.length - 1].equals(param) == false) {
                            htmlBuilder.append(", ");
                        }
                    }

                    htmlBuilder.append(")");

                    htmlBuilder.append("<br />");

                    Method[] methods = c.getDeclaredMethods();

                    htmlBuilder.append("<br /><b>Metode:</b><br />");

                    for (Method method : methods) {
                        int methodModifiers = method.getModifiers();

                        extractModifiers(methodModifiers, htmlBuilder);

                        htmlBuilder.append(method.getReturnType().getName());
                        htmlBuilder.append(" ");

                        htmlBuilder.append(method.getName());
                        htmlBuilder.append(" ");

                        Parameter[] methodParams = method.getParameters();

                        htmlBuilder.append("(");

                        for (Parameter param : methodParams) {
                            int paramModifiers = param.getModifiers();

                            extractModifiers(paramModifiers, htmlBuilder);

                            htmlBuilder.append(param.getType().getName());
                            htmlBuilder.append(" ");
                            htmlBuilder.append(param.getName());
                            if (methodParams[methodParams.length - 1]
                                    .equals(param) == false) {
                                htmlBuilder.append(", ");
                            }
                        }

                        htmlBuilder.append(")");
                        htmlBuilder.append("<br />");
                    }
                }

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void extractModifiers(int modifiers, StringBuilder htmlBuilder) {

        htmlBuilder
                .append(Modifier.toString(modifiers))
                .append(" ");

    }

    public static void findAllPackagesAndClasses(String directoryName, StringBuilder htmlBuilder) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (!file.isDirectory()) {
                String path = file.getPath();
                String packName = path.substring(path.indexOf("src") + 4, path.lastIndexOf('\\'));
                packName = packName.replace('\\', '.');

                documentClass(file.getName(), htmlBuilder, packName);
            } else if (file.isDirectory()) {

                findAllPackagesAndClasses(file.getAbsolutePath(), htmlBuilder);
            }
        }

    }

    private static void appendParent(Class<?> clazz, StringBuilder classInfo, boolean first) {
        Class<?> parent = clazz.getSuperclass();
        if (parent == null) {
            return;
        }
        if (first) {
            classInfo.append(System.lineSeparator());
            classInfo.append("<b>extends:</b><br />");
        }
        classInfo
                .append(" ")
                .append(parent.getName())
                .append("<br />");
        appendParent(parent, classInfo, false);
    }

    private static void appendInterfaces(Class<?> clazz, StringBuilder classInfo) {
        if (clazz.getInterfaces().length > 0) {
            classInfo.append(System.lineSeparator());

            classInfo.append("<b>implements:</b><br />");
        }
        for (Class<?> in : clazz.getInterfaces()) {
            classInfo
                    .append(" ")
                    .append(in.getName()
                    ).append("<br />");
        }
    }
}
