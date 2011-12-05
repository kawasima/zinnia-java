package net.unit8.zinnia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ajax.JSON;

public class CanvasServer {
	private static Recognizer recognizer;
	public static class RecognizeHandler extends AbstractHandler
	{
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
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
	        baseRequest.setHandled(true);
	        response.getWriter().println("[" + StringUtils.join(candidates, ",") + "]");
	    }
	}
	
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
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, new RecognizeHandler(), new DefaultHandler() });
        server.setHandler(handlers);
        server.start();
        server.join();
    }
}
