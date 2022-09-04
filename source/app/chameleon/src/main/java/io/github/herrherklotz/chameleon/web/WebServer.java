package io.github.herrherklotz.chameleon.web;

import android.content.res.AssetFileDescriptor;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import io.github.herrherklotz.chameleon.BuildConfig;
import io.github.herrherklotz.chameleon.MainService;
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper;
import io.github.herrherklotz.chameleon.x.utils.Storage;
import kotlin.Pair;

import static io.github.herrherklotz.chameleon.Chameleon.LOG_D;


public class WebServer extends NanoHTTPD {
	public static final int PORT = 8080;
	
	public static final ObjectWriter sJSONWriter = new MyObjectMapper().writer();
	
	public static final String MIME_JSON = "application/javascript";
	
	private static final String MIME_JAVASCRIPT = "application/x-javascript";
	
	
	public WebServer() {
		super(PORT);
		this.setTempFileManagerFactory(new TempFilesServer.ExampleManagerFactory());
	}
	
	
	public static Response error() {
		return error("");
	}
	
	
	public static Response error(String msg) {
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, msg);
	}
	
	
	public static Response error(Response.IStatus code, String msg) {
		return newFixedLengthResponse(code, MIME_PLAINTEXT, msg);
	}
	
	
	public static String getMimeType(String pFileName) {
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(pFileName);
		
		switch (fileExtension) {
			case "js":
				return MIME_JAVASCRIPT;
			default:
				return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
		}
	}
	
	
	public static Response missingParameter(String parameterName) {
		return error(Response.Status.NOT_FOUND, "missing '" + parameterName + "' in request!");
	}
	
	
	public static Response ok() {
		return ok("");
	}
	
	
	public static Response ok(String msg) {
		return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, msg);
	}
	
	
	public static String parseBodyToString(IHTTPSession session) {
		int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
		byte[] buf = new byte[contentLength];
		String result = null;
		
		try {
			session.getInputStream().read(buf, 0, contentLength);
			result = new String(buf);
		} catch (IOException e) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	
	@Override
	public Response serve(IHTTPSession session) {
		if (session.getUri().startsWith("/api")) { // serve back-end api
			try {
				String uri = session.getUri();
				
				uri = uri.replace(".", "Dot");
				uri = uri.replace("/", ".");
				uri += "Class";
				Class c = Class.forName("io.github.herrherklotz.chameleon.web" + uri);
				java.lang.reflect.Method method = c.getMethod("serve", IHTTPSession.class);
				
				return (Response) method.invoke(null, session);
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}  else if(session.getUri().startsWith("/screen")) {
			String lPath = "screen/";
			lPath += session.getUri().substring(8); // 8 == "/screen/".length()

			try {
				AssetFileDescriptor afd = MainService.Companion.getContext().getAssets().openFd(
						lPath);
				File lFile = new File(lPath);

				return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, WebServer.getMimeType(
						lFile.getName()), afd.createInputStream(), afd.getLength());
			} catch (IOException ignore) {}
		} else if (session.getUri().startsWith("/project/")) {
			String[]  lSplittedUri = session.getUri().split("/");

			if (lSplittedUri.length >= 3) {
				StringBuilder lPath = new StringBuilder("/Project/");
				lPath.append(lSplittedUri[2]); // project name
				lPath.append("/Frontend");

				for (int i = 3; i < lSplittedUri.length; i++)
					lPath.append("/").append(lSplittedUri[i]);

				Pair<Boolean, File> result = Storage.INSTANCE.loadFile(lPath.toString());

				if (result.getFirst()) {
					try {
						return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, WebServer.getMimeType(
								result.getSecond().getName()), new FileInputStream(result.getSecond()), result.getSecond().length());
					} catch (IOException ignore) {}
				}
			}
		} else if (session.getMethod() == NanoHTTPD.Method.GET) {
			// serve IDE
			// TODO fix for Angular Router or switch to different server
			String lPath = "www/";

			if (session.getUri().length() <= 1)
				lPath += "index.html";
			else
				lPath += session.getUri().substring(1);

			try {
				AssetFileDescriptor afd = MainService.Companion.getContext().getAssets().openFd(
						lPath);
				File lFile = new File(lPath);

				return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, WebServer.getMimeType(
						lFile.getName()), afd.createInputStream(), afd.getLength());
			} catch (IOException ignore) {}
		}
		
		return WebServer.error(Response.Status.NOT_IMPLEMENTED, "The requested URI (" +
				session.getUri() + ") doesn't exists!");
	}
}