

package com.watabou.glscripts;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.gl.Program;
import com.nyrds.platform.gl.Shader;

import java.util.HashMap;
import java.util.Map;

public class Script extends Program {

	private static final Map<Class<? extends Script>,Script> all =
			new HashMap<>();
	
	private static Script curScript = null;
	private static Class<? extends Script> curScriptClass = null;
	
	@SuppressWarnings("unchecked")
	public static<T extends Script> T use( Class<T> c ) {
		
		if (c != curScriptClass) {
			
			Script script = all.get( c );
			if (script == null) {
				try {
					script = c.newInstance();
				} catch (Exception e) {
					EventCollector.logException(e);
				}
				all.put( c, script );
			}
			
			if (curScript != null) {
				curScript.unuse();
			}
			
			curScript = script;
			curScriptClass = c;
			curScript.use();

		}
		
		return (T)curScript;
	}
	
	public static void reset() {
		for (Script script:all.values()) {
			script.delete();
		}
		all.clear();
		
		curScript = null;
		curScriptClass = null;
	}
	
	public void compile( String src ) {

		String[] srcShaders = src.split( "//\n" ); 
		attach( Shader.createCompiled( Shader.VERTEX, srcShaders[0] ) );
		attach( Shader.createCompiled( Shader.FRAGMENT, srcShaders[1] ) );
		link();

	}
	
	public void unuse() {
	}
}
