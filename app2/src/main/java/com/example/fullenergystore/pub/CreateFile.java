package com.example.fullenergystore.pub;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/3/10 0010.
 */
public class CreateFile {

    private Context context;
    public static String SD_CARD = Environment.getExternalStorageDirectory() + "/";;
    public static String SELFDIR = SD_CARD + "FullEnergy/";
    public static String HTML_DIR = SELFDIR + "HTML/";

    public CreateFile(Context context){
        init();
        this.context = context;
    }

    private void init() {

        if(ExistSDCard()){
            File sd = Environment.getExternalStorageDirectory();
            String filePath = SELFDIR;
            File file = new File(filePath);
            if(!file.exists()){
                file.mkdirs();
                System.out.println("在SD上创建FullEnergy文件夹!");
            }else{
                System.out.println("SD上FullEnergy文件夹以创建!");
            }

            String musicFilePath = HTML_DIR;
            File musicfile = new File(musicFilePath);
            if(!musicfile.exists()){
                musicfile.mkdirs();
                System.out.println("在FullEnergy文件夹里创建HTML文件夹!");
            }else{
                System.out.println("文件夹HTML在FullEnergy里已创建!");
            }

            File dir1 = new File(HTML_DIR + "TEXT.HTML");
            if (!dir1.exists()) {
                try {
                    //在指定的文件夹中创建文件
                    dir1.createNewFile();
                    System.out.println("测试文件创建成功！！");
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }else{
                System.out.println("测试文件已经被创建！");
            }

        }else {
            String filePath = "/data/data/ZeroStage/";
            File file = new File(filePath);
            if(file.exists()){
                System.out.println("项目目录文件夹以创建!");
            }else{
                file.mkdirs();
                System.out.println("在项目目录上创建文件夹!");
            }
        }
    }

    private boolean ExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("存在SD卡!");
            return true;
        } else
            System.out.println("不存在SD卡!");
            return false;
    }
    
    private String leftToRight(String path){
    	String newPath = null;
    	newPath = path.replace("/", "\\");
    	return newPath;
    }
}
