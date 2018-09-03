package com.haiwen.smart.framework;

import com.haiwen.smart.framework.bean.Data;
import com.haiwen.smart.framework.bean.Handler;
import com.haiwen.smart.framework.bean.Param;
import com.haiwen.smart.framework.bean.View;
import com.haiwen.smart.framework.helper.BeanHelper;
import com.haiwen.smart.framework.helper.ConfigHelper;
import com.haiwen.smart.framework.helper.ControllerHelper;
import com.haiwen.smart.framework.helper.HelperLoader;
import com.haiwen.smart.framework.helper.RequestHelper;
import com.haiwen.smart.framework.helper.UploadHelper;
import com.haiwen.smart.framework.util.ArrayUtil;
import com.haiwen.smart.framework.util.CodecUtil;
import com.haiwen.smart.framework.util.JsonUtil;
import com.haiwen.smart.framework.util.ReflectionUtil;
import com.haiwen.smart.framework.util.StreamUtil;
import com.haiwen.smart.framework.util.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

    @Override public void init(ServletConfig config) throws ServletException {
        HelperLoader.init();

        ServletContext servletContext = config.getServletContext();

        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
        UploadHelper.init(servletContext);
    }

    @Override protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String requestMethod = req.getMethod().toLowerCase();
        String requestPath = req.getPathInfo();

        if (requestPath.equals("/favicon.ico")) {
            return;
        }

        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);

        if (handler != null) {
            Class<?> controllerClass = handler.getControllerClass();
            Object controllerBean = BeanHelper.getBean(controllerClass);

            Param param;
            if (UploadHelper.isMultipart(req)) {
                param = UploadHelper.createParm(req);
            } else {
                param = RequestHelper.createParam(req);
            }

            Object result;

            Method actionMethod = handler.getActionMethod();
            if (param.isEmpty()) {
                result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
            } else {
                result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
            }

            if (result instanceof View) {
                handleViewResult((View)result, req, resp);
            } else if (result instanceof Data) {
                handleDataResult((Data)result, resp);
            }
        }
    }

    private void handleViewResult(View view, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        String path = view.getPath();
        if (StringUtil.isNotEmpty(path)) {
            if (path.startsWith("/")) {
                response.sendRedirect(request.getContextPath() + path);
            } else {
                Map<String, Object> model = view.getModel();
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                request.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(request, response);
            }
        }
    }

    private void handleDataResult(Data data, HttpServletResponse response) throws IOException {
        Object model = data.getModel();
        if (model != null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            String json = JsonUtil.toJSON(model);
            writer.write(json);
            writer.flush();
            writer.close();
        }
    }
}
