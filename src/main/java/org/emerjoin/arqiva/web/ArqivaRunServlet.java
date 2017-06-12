package org.emerjoin.arqiva.web;

import org.emerjoin.arqiva.core.Arqiva;
import org.emerjoin.arqiva.core.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Mário Júnior
 */
@WebServlet(name = "ArqivaRun",urlPatterns = "/*",loadOnStartup = 1)
public class ArqivaRunServlet extends HttpServlet {

    private Arqiva arqivaInstance = null;
    private static Logger log = LoggerFactory.getLogger(ArqivaRunServlet.class);

    public ArqivaRunServlet(){ }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if(Middleware.INVALIDATE_TOPICS_TREE)
            Middleware.ARQIVA_PROJECT.invalidateTopicsTree();

        String resourceRequested = req.getRequestURI().substring(req.getContextPath().length()+1,req.getRequestURI().length());

        log.debug("Resource requested: "+resourceRequested);
        if(resourceRequested.equals("index.html")){
            renderIndexPage(req,resp);
            return;
        }

        if(resourceRequested.startsWith("topics/"))
            renderTopicPage(resourceRequested,req,resp);
        else resp.sendError(404);

    }

    private void renderIndexPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String startPoint = arqivaInstance.getStartPoint();
        String templatePageHtml = "";

        if(startPoint.equals(Arqiva.START_POINT_INDEX)){
            templatePageHtml =  arqivaInstance.renderIndexPage();
        }else if(startPoint.equals(Arqiva.START_POINT_FIRST_TOPIC)){
            TreeNode topic =  arqivaInstance.getProject().getTopicsTree().firstTopic();
            templatePageHtml = arqivaInstance.renderTopicPage(topic.getRef().getUrl(),true);
        }else{
            templatePageHtml = arqivaInstance.renderTopicPage(startPoint,true);
        }

        writeHtml(templatePageHtml,resp);

    }


    private void renderTopicPage(String topic, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        emitHeaders(resp);
        String topicPageHtml = arqivaInstance.renderTopicPage(topic.replace(".html","").replace("topics/",""));
        writeHtml(topicPageHtml,resp);


    }

    public void init(){

        log.info("Initializing ArqivaRunServlet...");

        arqivaInstance = new Arqiva(Middleware.ARQIVA_PROJECT);
        log.info("Preparing Arqiva instance...");
        arqivaInstance.getReady();

    }

    private void emitHeaders(HttpServletResponse response) throws ServletException,IOException{

        response.setContentType("text/html");

    }

    private void writeHtml(String html, HttpServletResponse response) throws ServletException, IOException {

        emitHeaders(response);
        //log("Writing "+html.length()+" characters out");
        PrintWriter printWriter = response.getWriter();
        printWriter.print(html);
        printWriter.flush();
        printWriter.close();

    }

}
