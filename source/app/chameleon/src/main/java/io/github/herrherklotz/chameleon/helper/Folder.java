package io.github.herrherklotz.chameleon.helper;

import java.io.File;
import java.util.HashMap;

public class Folder extends HashMap<String, Object> {
	public Folder(File pFile) {
		super();
		
		for (File item : pFile.listFiles()) {
			if (item.isDirectory()) {
				this.put(item.getName(), new Folder(item));
			} else {
				this.put(item.getName(), null);
			}
		}
	}
}
