package com.pbq.vote.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class WebUtil {

    public static void writeOutput(Environment env, HttpServletRequest request, HttpServletResponse response, InputStream inputStream, long length) throws IOException {
        String acceptEnc = request.getHeader("Accept-Encoding");
        response.setCharacterEncoding("UTF-8");
        //先将acceptEnc置空，即不压缩返回内容，待对接前端之后在进行解压缩操作
        acceptEnc = "";
        if (StrUtil.isNotEmpty(acceptEnc) && (length > 1024 || length < 0)) {
            InputStream in = inputStream;
            OutputStream out = response.getOutputStream();
            GZIPOutputStream zout = new GZIPOutputStream(out, true);
            StreamUtils.copy(in,zout);
        }else{
            response.setContentLength((int)length);
            InputStream in = inputStream;
            OutputStream out = response.getOutputStream();
            StreamUtils.copy(in,out);
        }

    }
}
