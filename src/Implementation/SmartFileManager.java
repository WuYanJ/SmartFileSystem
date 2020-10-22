package Implementation;

import Interfaces.Id;

import java.io.*;

public class SmartFileManager implements Interfaces.FileManager {
    SmartId fmId;

    public SmartFileManager(SmartId id) {
        this.fmId = id;
    }

    public SmartId getFmId() {
        return fmId;
    }

    public void setFmId(SmartId fmId) {
        this.fmId = fmId;
    }

    @Override
    public SmartFile getFile(Id fileId) {
        return new SmartFile((SmartId) fileId, this, null);
    }

    @Override
    public SmartFile newFile(Id fileId) {
        File newFile = new File("./FileManager/" + fmId.getId() + "/" + ((SmartId) fileId).getId() + ".meta");
        SmartFile newSmartFile = new SmartFile((SmartId) fileId, this, null);
        if (!newFile.exists()) {
            //先得到文件的上级目录，并创建上级目录，在创建文件
            newFile.getParentFile().mkdir();
            try {
                //创建文件
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newSmartFile;
    }

    public SmartFile getFileByName(String fileName) throws IOException, ClassNotFoundException {
        File[] list = new File("/Users/wuyanjie/IdeaProjects/SmartFileSystem/FileManager/" + fmId.getId()).listFiles();
        ObjectInputStream ois;
        SmartFileMeta fileMeta;
        for (File file : list) {
            ois = new ObjectInputStream(new FileInputStream(file));
            fileMeta = (SmartFileMeta) ois.readObject();
            if (fileMeta.fileName != null && fileName.contentEquals(fileMeta.fileName)) {
                SmartId fId = new SmartId(file.getName().substring(0, fileName.length() - 6));
                return getFile(fId);
            }
        }
//        在自己这个FM下，新随机分配一个fx.meta
        System.out.println("创建文件从头开始写");
        SmartId newFileId = new SmartId("f" + list.length);
        File newMetaFile = new File("./FileManager/" + fmId.getId() + "/" + newFileId.getId() + ".meta");
        // filename写进fx.meta，用相应的fId getFile返回
        SmartFileMeta newFileMeta = new SmartFileMeta(null, fileName, 0, null, null);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newMetaFile));
        oos.writeObject(newFileMeta);
        oos.close();
        return new SmartFile(newFileId, this, null);
    }
}
