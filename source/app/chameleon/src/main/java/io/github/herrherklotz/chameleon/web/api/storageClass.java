package io.github.herrherklotz.chameleon.web.api;

import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.github.herrherklotz.chameleon.BuildConfig;
import io.github.herrherklotz.chameleon.MainService;
import io.github.herrherklotz.chameleon.helper.Folder;
import io.github.herrherklotz.chameleon.web.WebServer;

import static io.github.herrherklotz.chameleon.Chameleon.LOG_D;
import static io.github.herrherklotz.chameleon.x.bridge.BridgeKt.elementToPath;

public class storageClass {
	/**
	 * Delete a file or folder from the phone.
	 * <p>
	 * REQUEST-headers:
	 * m-path
	 * <p>
	 * RESPONSE-headers:
	 * -
	 *
	 * @param session Session
	 * @return Reponse
	 */
	private static Response delete(IHTTPSession session) {
		String lPath = session.getHeaders().get("m-path");
		LOG_D("/api/storage", "delete", lPath);
		
		File file = new File(MainService.Companion.getContext().getFilesDir(), lPath);
		
		try {
			FileUtils.forceDelete(file); // TODO check Java8
			
			return WebServer.ok("");
		} catch (IOException ignore) {
			return WebServer.error("ERROR - couldn't delete " + session.getHeaders().get("m-path"));
		}
	}
	
	
	private static Response get(IHTTPSession session) {
		Response lResponse;
		String lPath = session.getHeaders().get("m-path");
		// TODO check with regex
		lPath = elementToPath(lPath);

		if (lPath.endsWith("/")) {
			Folder lFolder = new Folder(new File(MainService.Companion.getContext().getFilesDir(), lPath));
			
			try {
				lResponse = WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_JSON, WebServer.sJSONWriter.writeValueAsString(lFolder));
			} catch (JsonProcessingException ignore) {
				lResponse = WebServer.error(Response.Status.NOT_FOUND, "File not exists");
			}
		} else {
			try {
				File file = new File(MainService.Companion.getContext().getFilesDir(), lPath);
				
				FileInputStream fis = new FileInputStream(file);//(MainService.Companion.getContext().getFilesDir() + mPath);
				lResponse = WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_PLAINTEXT, fis, fis.available());
				
				MimeTypeMap map = MimeTypeMap.getSingleton();
				String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
				String mime = map.getMimeTypeFromExtension(ext);
				if (mime == null) {
					mime = "*/*";
				}
				
				lResponse.setMimeType(mime);
				
				lResponse.addHeader("m-size", String.valueOf(file.length()));
				lResponse.addHeader("m-r", String.valueOf(file.canRead()));
				lResponse.addHeader("m-w", String.valueOf(file.canWrite()));
				lResponse.addHeader("m-x", String.valueOf(file.canExecute()));
				lResponse.addHeader("m-modified", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(file.lastModified())));
			} catch (IOException ignore) {
				lResponse = WebServer.error(Response.Status.NOT_FOUND, "File not exists");
			}
		}
		
		// result.addHeader("Access-Control-Allow-Origin", "*"); // TODO only for testing
		
		return lResponse;
	}
	
	
	/**
	 * REQUEST-headers:
	 * m-path
	 * <p>
	 * RESPONSE-headers:
	 * m-location
	 * m-mime
	 * m-modified
	 * m-path
	 * m-r
	 * m-size
	 * m-w
	 * m-x
	 *
	 * @param session Session
	 * @return Reponse
	 */
	private static Response head(IHTTPSession session) {
		Response lResponse;
		String lPath = session.getHeaders().get("m-path");
		LOG_D("/api/storage", "head", lPath);
		
		// Response lResponse = WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_PLAINTEXT, "");
		
		// if (!session.getHeaders().containsKey("m-path")) {
		// 	return WebServer.error("");
		// }
		
		// String lPath = session.getHeaders().get("m-path"); // Environment.getRootDirectory().getAbsolutePath();
		// LOG_D("/api/storage", "head", lPath);
		
		File file = new File(lPath);
		
		if (file.exists()) {
			lResponse = WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_PLAINTEXT, "");
			if (file.isDirectory()) {
				lResponse.addHeader("m-location", file.getPath());
				lResponse.addHeader("m-path", file.getPath() + "/");
				lResponse.addHeader("m-mime", "application/folder");
			} else {
				lResponse.addHeader("m-location", file.getParent());
				lResponse.addHeader("m-path", file.getPath());
				
				MimeTypeMap map = MimeTypeMap.getSingleton();
				String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
				String mime = map.getMimeTypeFromExtension(ext);
				if (mime == null) mime = "*/*";
				lResponse.addHeader("m-mime", mime);
			}
			
			lResponse.addHeader("m-size", String.valueOf(file.length()));
			lResponse.addHeader("m-r", String.valueOf(file.canRead()));
			lResponse.addHeader("m-w", String.valueOf(file.canWrite()));
			lResponse.addHeader("m-x", String.valueOf(file.canExecute()));
			lResponse.addHeader("m-modified", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(file.lastModified())));
			
			return lResponse;
		}
		
		return WebServer.error("");
	}
	
	
	/**
	 * Used to "execute" a file on the phone. Android will be choose a app to open the corresponding file (e.g. image viewer for a photo)
	 *
	 * @param session
	 * @return
	 */
	private static Response post(IHTTPSession session) {
		String fullpath;

		if(session.getParameters().containsKey("dir")) {
			fullpath = session.getParameters().get("dir").get(0);
		} else {
			fullpath = "/";
		}
		
		File file = new File(fullpath);
		MimeTypeMap map = MimeTypeMap.getSingleton();
		String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
		String type = map.getMimeTypeFromExtension(ext);

		if(type == null)
			type = "*/*";

		if(BuildConfig.DEBUG) LOG_D("/api/storage: " + type);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(file);
		intent.setDataAndType(data, type);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainService.Companion.getContext().startActivity(intent);

		return WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_PLAINTEXT, "");
	}
	
	
	private static Response put(IHTTPSession session) {
		Response result;
		String lPath = session.getHeaders().get("m-path");
		LOG_D("/api/storage", "put", lPath);
		
		if (lPath.endsWith("/")) {
			File file = new File(MainService.Companion.getContext().getFilesDir(), lPath);

			if (file.mkdirs()) {
				result = WebServer.ok("");
			} else {
				result = WebServer.error("");
			}
		} else {
			String mBody = WebServer.parseBodyToString(session);
			
			try {
				File file = new File(MainService.Companion.getContext().getFilesDir(), lPath);

				file.getParentFile().mkdirs();

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(mBody.getBytes());
				fos.close();
				
				result = WebServer.newFixedLengthResponse(Response.Status.OK, WebServer.MIME_PLAINTEXT, "");
			} catch (IOException e) {
				e.printStackTrace();
				result = WebServer.error(Response.Status.INTERNAL_ERROR, "Error during write to file!");
			}
		}
		
		return result;
	}
	
	
	public static Response serve(IHTTPSession session) {
		if (!session.getHeaders().containsKey("m-path")) {
			return WebServer.missingParameter("m-path");
		}
		
		switch (session.getMethod()) {
			case DELETE:
				return delete(session);
			case GET:
				return get(session);
			// case HEAD:
			//	    return head(session);
			// case POST:
			//  	return post(session);
			case PUT:
				return put(session);
			default:
				return WebServer.error("Not implemented");
		}
		
	}
}
