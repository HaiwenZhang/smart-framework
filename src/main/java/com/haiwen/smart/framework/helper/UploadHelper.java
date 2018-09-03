package com.haiwen.smart.framework.helper;

import com.haiwen.smart.framework.bean.FileParam;
import com.haiwen.smart.framework.bean.FormParam;
import com.haiwen.smart.framework.bean.Param;
import com.haiwen.smart.framework.util.CollectionUtil;
import com.haiwen.smart.framework.util.FileUtil;
import com.haiwen.smart.framework.util.StreamUtil;
import com.haiwen.smart.framework.util.StringUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadHelper {
    private static final Logger logger = LoggerFactory.getLogger(UploadHelper.class);

    private static ServletFileUpload servletFileUpload;

    public static void init(ServletContext servletContext) {
        File repository = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
        servletFileUpload =
            new ServletFileUpload(new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository));

        int uploadLimit = ConfigHelper.getAppUploadLimit();

        if (uploadLimit != 0) {
            servletFileUpload.setFileSizeMax(uploadLimit * 1024 * 1024);
        }
    }

    public static boolean isMultipart(HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(request);
    }

    public static Param createParm(HttpServletRequest request) throws IOException {
        List<FormParam> formParamList = new ArrayList<>();
        List<FileParam> fileParamList = new ArrayList<>();

        try {
            Map<String, List<FileItem>> fileItemListMap = servletFileUpload.parseParameterMap(request);
            if (CollectionUtil.isNotEmpty((Collection<?>)fileItemListMap)) {
                for (Map.Entry<String, List<FileItem>> fileItemListEntry : fileItemListMap.entrySet()) {
                    String fieldName = fileItemListEntry.getKey();
                    List<FileItem> fileItemList = fileItemListEntry.getValue();
                    if (CollectionUtil.isNotEmpty(fileItemList)) {
                        for (FileItem fileItem : fileItemList) {
                            if (fileItem.isFormField()) {
                                String fieldValue = fileItem.getString("UTF-8");
                                formParamList.add(new FormParam(fieldName, fieldValue));
                            } else {
                                String fileName = FileUtil.getRealFileName(
                                    new String(fileItem.getName().getBytes(), "UTF-8"));

                                if (StringUtil.isNotEmpty(fileName)) {
                                    long fileSize =fileItem.getSize();
                                    String contenType = fileItem.getContentType();
                                    InputStream inputStream = fileItem.getInputStream();

                                    fileParamList.add(
                                        new FileParam(fieldName, fileName, fileSize, contenType, inputStream));
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileUploadException e) {
            logger.error("create param failure ", e);
            throw new RuntimeException(e);
        }

        return new Param(formParamList, fileParamList);
    }


    public static void uploadFile(String basePath, FileParam fileParam) {
        try {
            if (fileParam != null) {
                String filePath = basePath + fileParam.getFileName();
                FileUtil.createFile(filePath);
                InputStream inputStream = new BufferedInputStream(fileParam.getInputStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                StreamUtil.copyStream(inputStream, outputStream);
            }
        } catch (Exception e) {
            logger.error("upload file failure ", e);
            throw new RuntimeException(e);
        }
    }

    public static void uploadFile(String basePath, List<FileParam> fileParamList) {
        try {
            if (CollectionUtil.isNotEmpty(fileParamList)) {
                for (FileParam fileParam : fileParamList) {
                    uploadFile(basePath, fileParam);
                }
            }
        } catch (Exception e) {
            logger.error("upload file failure ", e);
            throw new RuntimeException(e);
        }
    }
}
