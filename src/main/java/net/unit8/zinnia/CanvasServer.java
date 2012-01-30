package net.unit8.zinnia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ajax.JSON;

public class CanvasServer {
	private static Recognizer recognizer;

    public static void main(String[] args) throws Exception {
        Server server = new Server();
    	Connector connector = new SocketConnector();
    	connector.setPort(9999);
        server.addConnector(connector);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase("src/main/webapp");

        recognizer = new Recognizer();
        recognizer.open(new File("handwriting-ja.model"));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new RecognizeServlet()),"/recognize/*");
        context.addServlet(new ServletHolder(new TrainServlet()),"/train/*");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, context, new DefaultHandler() });
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    public static class RecognizeServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
    	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	    	Object[] strokes = (Object[])JSON.parse(request.getParameter("strokes"));
	    	int x = Integer.parseInt(request.getParameter("x"));
	    	int y = Integer.parseInt(request.getParameter("y"));
	    	Character c = new Character(x, y);
	    	for(int i=0; i<strokes.length; i++) {
	    		Object[] stroke = (Object[])strokes[i];
	    		for(int j=0; j < stroke.length; j++) {
	    			Object[] dot = (Object[])stroke[j];
	    			c.add(i, ((Long)dot[0]).intValue(), ((Long)dot[1]).intValue());
	    		}
	    	}
	    	Result result = recognizer.classify(c, 10);
	    	List<String> candidates = new ArrayList<String>(10);
	    	for(Pair<Double, String> pair:result.getResults()) {
	    		candidates.add("\"" + pair.second + "\"");
	    	}

	        response.setContentType("application/json;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("[" + StringUtils.join(candidates, ",") + "]");
    	}
    }

    public static class TrainServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
    	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	    	Object[] strokes = (Object[])JSON.parse(request.getParameter("strokes"));
	    	int x = Integer.parseInt(request.getParameter("x"));
	    	int y = Integer.parseInt(request.getParameter("y"));
	    	Character c = new Character(x, y);
	    	for(int i=0; i<strokes.length; i++) {
	    		Object[] stroke = (Object[])strokes[i];
	    		for(int j=0; j < stroke.length; j++) {
	    			Object[] dot = (Object[])stroke[j];
	    			c.add(i, ((Long)dot[0]).intValue(), ((Long)dot[1]).intValue());
	    		}
	    	}
	    	c.setValue(request.getParameter("c"));

	    	PrintStream out = null;
	    	try {
	    		out = new PrintStream(new FileOutputStream("handwriting-ja.s", true));
	    		out.println(c.toString());
	    	} catch(IOException e) {
	    		throw e;
	    	} finally {
	    		IOUtils.closeQuietly(out);
	    	}
	        response.setContentType("application/json;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("{\"message\": \"I learned.\"}");
    	}
    }
}
