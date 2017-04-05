package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MyClass {
    public static void main(String[] args) {
        //需要添加的前缀
        String resourcePrefix = "siyehua_";
        //需要重命名的module main路径
        File srcPath = new File("C:\\as\\AutoReNameModuleRes\\target1\\src\\main");

        //下面是替换规则,也自定义添加修改,也可维持不变
        /**
         * java code replace rule
         */
        String[] resArray = new String[]{
                "R.anim.",
                "R.animator.",
                "R.array.",
                "R.attr.",
                "R.bool.",
                "R.color.",
                "R.dimen.",
                "R.drawable.",
                "R.id.",
                "R.integer.",
                "R.layout.",
                "R.menu.",
                "R.mipmap.",
                "R.plurals.",
                "R.raw.",
                "R.string.",
                "R.style.",
                "R.transition.",
                "R.xml"};
        /**
         * xml reference replace rule
         */
        String[] valuesArray = new String[]{
                "@anim/",
                "@animator/",
                "@array/",
                "@attr/",
                "@bool/",
                "@color/",
                "@dimen/",
                "@drawable/",
                "@id/",
                "@+id/",
                "@integer/",
                "@layout/",
                "@menu/",
                "@mipmap/",
                "@plurals/",
                "@raw/",
                "@string/",
                "@style/",
                "@transition/",
                "@xml/"};
        /**
         * xml definition replace rule
         */
        String[] xmlArray = new String[]{
                "<string-array ",
                "<integer-array ",
                "<integer-array ",
                "<array ",
                "<declare-styleable ",
                "<bool ",
                "<color ",
                "<dimen ",
                "<item type=\"id\" ",
                "<integer ",
                "<string ",
                "<style "};

        startCheck(resourcePrefix, srcPath, resArray, valuesArray, xmlArray);
    }

    /**
     * start check module cod and resources
     *
     * @param key         res resourcePrefix
     * @param file        src path
     * @param resArray    java cod replace rule
     * @param valuesArray xml references rule
     * @param xmlArray    xml definition rule
     */
    public static void startCheck(String key, File file, String[] resArray,
                                  String[] valuesArray, String[] xmlArray) {
        //fix java code
        File javaPath = new File(file.getPath() + File.separator + "java");
        checkJava(key, javaPath, resArray);

        //fix res without values
        File resPath = new File(file.getPath() + File.separator + "res");
        checkRes(key, resPath, valuesArray);

        //fix values
        checkValues(key, resPath, valuesArray, xmlArray);

        //fix AndroidManifest.xml
        checkXmlFile(key, new File(file.getPath() + File.separator + "AndroidManifest.xml"),
                valuesArray);
    }

    /**
     * fix values
     *
     * @param key         res resourcePrefix
     * @param dir         res path
     * @param valuesArray xml references rule
     * @param xmlArray    xml definition rule
     */
    public static void checkValues(String key, File dir, String[] valuesArray, String[] xmlArray) {
        if (dir.isDirectory()) {
            File[] tmpList = dir.listFiles();
            for (File aTmpList : tmpList) {
                checkValues(key, aTmpList, valuesArray, xmlArray);
            }
        } else if (dir.getParentFile().getName().startsWith("values") &&
                dir.isFile() && dir.getName().endsWith(".xml")) {
            /**
             * fix references
             */
            checkXmlFile(key, dir, valuesArray);
            /**
             * fix definition
             */
            checkValues(key, dir, xmlArray);

        }
    }

    /**
     * fix values definition
     *
     * @param key      res resourcePrefix
     * @param file     xml file
     * @param xmlArray xml definition rule
     */
    public static void checkValues(String key, File file, String[] xmlArray) {
        try {


            String tag = file.getPath();

            boolean ifChange = false;

            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8"));
            String content = "";
            while (true) {
                String str = reader.readLine();
                if (str != null) {
                    for (String aXmlArray : xmlArray) {
                        int start = str.indexOf(aXmlArray);
                        int start2 = str.indexOf("name=\"");
                        if (start >= 0 && start2 >= 0) {
                            String resName = str.substring(start2 + 6);
                            if (!resName.startsWith(key)) {
                                System.out.println(tag);
                                System.out.println(aXmlArray + "old content: " + str);
                                ifChange = true;
                                resName = key + resName;
                                str = str.substring(0, start2) + "name=\"" + resName;
                                System.out.println("new content: " + str + "\n");
                            }
                            break;
                        }
                    }
                    content += str + "\n";
                } else
                    break;
            }

            is.close();

            if (!ifChange) {
                return;
            }

            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            outputStream.write(content);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fix file name and references
     *
     * @param key         res resourcePrefix
     * @param dir         res path
     * @param valuesArray xml references rule
     */
    public static void checkRes(String key, File dir, String[] valuesArray) {
        if (dir.isDirectory()) {
            File[] tmpList = dir.listFiles();
            for (File aTmpList : tmpList) {
                checkRes(key, aTmpList, valuesArray);
            }
        } else {
            if (!dir.getParentFile().getName().startsWith("values")) {
                /**
                 * fix xml references rule
                 */
                if (dir.getName().endsWith(".xml")) {
                    checkXmlFile(key, dir, valuesArray);
                }
                /**
                 * fix file name
                 */
                reNameResFile(key, dir);
            }

        }
    }

    /**
     * fix res name without values dir
     *
     * @param key  res resourcePrefix
     * @param file xml file
     */
    public static void reNameResFile(String key, File file) {
        if (!file.getName().startsWith(key)) {
            String newName = file.getParent() + File.separator + key + file.getName();
            System.out.println(file);
            System.out.println("old name: " + file.getParent() + File.separator + file.getName());
            System.out.println("new name: " + newName + "\n");
            file.renameTo(new File(newName));
        }
    }

    /**
     * fix file content with rules
     *
     * @param key         res resourcePrefix
     * @param file        xml file
     * @param valuesArray rules
     */
    public static void checkXmlFile(String key, File file, String[] valuesArray) {
        try {
            String tag = file.getPath();
            boolean ifChange = false;

            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8"));
            String content = "";
            while (true) {
                String str = reader.readLine();
                if (str != null) {
                    for (String aValuesArray : valuesArray) {
                        int start = str.indexOf(aValuesArray);
                        if (start >= 0) {
                            String resName = str.substring(start + aValuesArray.length());
                            if (!resName.startsWith(key)) {
                                System.out.println(tag);
                                System.out.println("old content: " + str);
                                ifChange = true;
                                resName = key + resName;
                                str = str.substring(0, start) + aValuesArray + resName;
                                System.out.println("new content: " + str + "\n");
                            }
                            break;
                        }
                    }
                    content += str + "\n";
                } else
                    break;
            }

            is.close();

            if (!ifChange) {
                return;
            }

            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            outputStream.write(content);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * fix java code
     *
     * @param key      res resourcePrefix
     * @param dir      java root
     * @param resArray java code rule
     */
    public static void checkJava(String key, File dir, String[] resArray) {
        if (dir.isDirectory()) {
            File[] tmpList = dir.listFiles();
            for (File aTmpList : tmpList) {
                checkJava(key, aTmpList, resArray);
            }
        } else {
            checkJavaFile(key, dir, resArray);
        }
    }

    /**
     * fix java file
     *
     * @param key      res resourcePrefix
     * @param file     java file
     * @param resArray java code rule
     */
    public static void checkJavaFile(String key, File file, String[] resArray) {
        try {

            String tag = file.getPath();
            boolean ifChange = false;

            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8"));
            String content = "";
            while (true) {
                String str = reader.readLine();

                if (str != null) {
                    if (str.contains("android.R.")) {
                        continue;
                    }
                    if (str.startsWith("import android.R;")) {
                        System.out.println("You should use android.R.XXX replace R.XXX in "
                                + tag + "\n");
                        return;
                    }
                    for (String aResArray : resArray) {
                        int start = str.indexOf(aResArray);
                        if (start >= 0) {
                            String resName = str.substring(start + aResArray.length());
                            if (!resName.startsWith(key)) {
                                System.out.println(tag);
                                System.out.println("old content: " + str);
                                ifChange = true;
                                resName = key + resName;
                                str = str.substring(0, start) + aResArray + resName;
                                System.out.println("new content: " + str + "\n");
                            }
                            break;
                        }
                    }
                    content += str + "\n";
                } else
                    break;
            }

            is.close();

            if (!ifChange) {
                return;
            }


            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            outputStream.write(content);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
