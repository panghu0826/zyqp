package com.buding.common.admin.cmd;

import groovy.lang.GroovyShell;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.common.util.IOUtil;
import com.buding.common.util.ObjectUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroovyCmd extends BaseCmd {
	private Logger logger = LogManager.getLogger(getClass());
	GroovyBinding binding;
	GroovyShell shell;
	Map<String, Object> vars;
	Map<String, Object> initVars;
	LinkedList<Object> paths = new LinkedList<Object>();
	LinkedList<String> pathNames = new LinkedList<String>();

	public GroovyCmd(Map<String, Object> env) {
		binding = new GroovyBinding();
		shell = new GroovyShell(binding);
		initVars = env;
		vars = new HashMap<String, Object>();
		init();
	}

	public void init() {
		clearVars();
		clearPaths();
		vars.putAll(initVars);
	}

	@Override
	public void showTips(PrintWriter writer) throws Exception {
		writer.println("$_>");
		writer.flush();
	}

	@Override
	public ExecuteCmdResult executeCmdOnSelect(String line, PrintWriter writer) throws Exception {
		String strs[] = line.split(" ");
		String cmd = strs[0];
		// 以下是模拟redis命令
		if ("clear".equals(cmd)) {
			clearCmd(writer, cmd, null, null);
		} else if ("get".equals(cmd)) {
			get(writer, cmd, strs[1]);
		} else if ("elget".equals(cmd)) {
			elget(writer, cmd, strs[1]);
		} else if ("set".equals(cmd)) {
			setCmd(writer, cmd, strs[1], strs[2]);
		} else if ("elset".equals(cmd)) {
			elset(writer, cmd, strs[1], strs[2]);
		} else if ("invoke".equals(cmd)) {
			invoke(writer, cmd, strs[1]);
		} else if ("sinvoke".equals(cmd)) {
//			invoke(writer, cmd, strs);
		} else if ("elinvoke".equals(cmd)) {
			elinvoke(writer, cmd, strs[1], strs[2]);
		} else if ("keys".equals(cmd)) {
			keysCmd(writer, cmd);
		} else if ("hkeys".equals(cmd)) {
			hkeys(writer, cmd, strs[1]);
		} else if ("hset".equals(cmd)) {
			hset(writer, cmd, strs[1], strs[2]);
		} else if ("hdel".equals(cmd)) {
			hdel(writer, cmd, strs[1]);
		} else if ("list".equals(cmd)) {
			listCmd(writer, cmd);
		} else if ("del".equals(cmd)) {
			delCmd(writer, cmd, strs[1]);
		} else if ("new".equals(cmd)) {
			newObj(writer, cmd, strs[1]);
		} else if("ms".equals(cmd)) {
			msCmd(writer, cmd);
		} else if("reflect".equals(cmd)) {
//			reflect(writer, cmd);
		}
		// 以下是模拟linux命令
		else if ("ls".equals(cmd)) {
			lsCmd(writer, cmd);
		} else if ("pwd".equals(cmd)) {
			pwdCmd(writer, cmd);
		} else if ("cd".equals(cmd)) {
			cd(writer, cmd, strs[1]);
		} else if ("cp".equals(cmd)) {
			cp(writer, cmd, strs[1], strs[2]);
		} else if ("cat".equals(cmd)) {
			cat(writer, cmd, strs[1]);
		} else if ("type".equals(cmd)) {
			type(writer, cmd, strs[1]);
		} else if ("load".equals(cmd)) {
			load(writer, cmd, strs[1], strs[2], strs[3]);
		} else if ("dump".equals(cmd)) {
			dump(writer, cmd, strs[1], strs[2]);
		}
		return ExecuteCmdResult.CONTINUE;
	}

	/**
	 * get matchSrv
	 * 
	 * @param cmd
	 * @param key
	 * @param writer
	 */
	public void get(PrintWriter writer, String cmd, String key) {
		Object obj = vars.get(key);
		if (obj == null) {
			writer.println("null");
		} else {
			writer.println(obj.toString());
		}
	}

	public Object getByEL(Object obj, Queue<String> keys) throws Exception {
		if (obj == null) {
			return null;
		}

		if (keys.isEmpty()) {
			return obj;
		}
		String key = keys.poll();
		if (StringUtils.isBlank(key)) {
			return obj;
		}

		Pattern p = Pattern.compile("(.+?)\\[(.+?)\\]");
		Matcher m = p.matcher(key);
		// 处理没有[]的
		if (!m.find()) {
			Field f = obj.getClass().getDeclaredField(key);
			f.setAccessible(true);
			obj = f.get(obj);
			return getByEL(obj, keys);
		}

		String k1 = m.group(1);
		String k2 = m.group(2);

		Field f = obj.getClass().getDeclaredField(k1);
		f.setAccessible(true);
		obj = f.get(obj);

		if (obj == null) {
			return null;
		}

		// 处理pool["k1"]这种类型
		if (k2.indexOf("\"") != -1) {// 字符串,只对Map使用
			k2 = k2.replaceAll("\"", "");
			obj = ((Map) obj).get(k2);
			return getByEL(obj, keys);
		}

		// 处理pool["k1"]这种类型
		if (k2.indexOf("\'") != -1) {// 字符串,只对Map使用
			k2 = k2.replaceAll("\'", "");
			obj = ((Map) obj).get(k2);
			return getByEL(obj, keys);
		}

		// 处理pool[1]纯数字类型
		if (Pattern.matches("^\\d+$", k2)) {
			Class cls = obj.getClass();
			if (List.class.isAssignableFrom(cls)) {
				obj = ((List) obj).get(Integer.valueOf(k2));
				return getByEL(obj, keys);
			}
			if (Map.class.isAssignableFrom(cls)) {
				obj = ((Map) obj).get(Integer.valueOf(k2));
				return getByEL(obj, keys);
			}
			if (cls.isArray()) {
				obj = Array.get(obj, Integer.valueOf(k2));
				return getByEL(obj, keys);
			}
			throw new RuntimeException("无法处理的el表达式:" + key + ", 目标类型:" + cls);
		}

		// 处理pool[k1]这种类型变量类型,只对Map使用
		Object k = getByEL(k2);
		obj = ((Map) obj).get(k);
		return getByEL(obj, keys);
	}

	public Object getByEL(String key) throws Exception {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		String keys[] = key.split("\\.");
		Queue<String> queue = new LinkedList<String>();
		for (String kk : keys) {
			queue.add(kk);
		}

		String k1 = queue.poll();
		Object obj = vars.get(k1);
		return getByEL(obj, queue);
	}

	/**
	 * dget matchSrv
	 * 
	 * @param cmd
	 * @param key
	 * @param writer
	 */
	public void elget(PrintWriter writer, String cmd, String key) throws Exception {
		Object obj = getByEL(key);

		if (obj == null) {
			writer.println("null");
		} else {
			Object deepObj = ObjectUtil.deepToString(obj);
			writer.println(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(deepObj));
		}
	}

	public void lsCmd(PrintWriter writer, String cmd) throws Exception {
		if (paths.isEmpty()) {
			throw new RuntimeException("invalid data");
		}
		Object obj = paths.getLast();
		printKeys(writer, obj);
	}

	public void pwdCmd(PrintWriter writer, String cmd) {
		String path = "";
		Iterator<String> iter = pathNames.iterator();
		while (iter.hasNext()) {
			path = path + "/" + iter.next();
		}
		path = path.substring(1);
		writer.println(path);
		writer.flush();
	}

	public Object getByPath(PrintWriter writer, String cmd, String paths) throws Exception {
		Object obj = null;
		if (paths.startsWith("/")) {
			obj = vars;
			paths = paths.substring(1);
		} else {
			obj = this.paths.getLast();
		}
		for (String path : paths.split("/")) {
			obj = getByPath(writer, cmd, path, obj);
			if (obj == null) {
				return null;
			}
		}
		return obj;
	}

	public Object getByPath(PrintWriter writer, String cmd, String path, Object obj) throws Exception {
		if (".".equals(path)) {
			return obj;
		}
		if ("..".equals(path)) {
			writer.println("-bash: cd: .. is not  supported");
			writer.flush();
			return null;
		}
		return getByNextPath(writer, cmd, path, obj);
	}

	public void cat(PrintWriter writer, String cmd, String path) throws Exception {
		Object obj = this.getByPath(writer, cmd, path);
		writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
		writer.flush();
	}

	public void type(PrintWriter writer, String cmd, String path) throws Exception {
		Object obj = this.getByPath(writer, cmd, path);
		if (obj == null) {
			writer.println("null");
			return;
		}
		writer.println(obj.getClass().getName());
		writer.flush();
	}

	public void cp(PrintWriter writer, String cmd, String path, String key) throws Exception {
		Object obj = this.getByPath(writer, cmd, path);
		vars.put(key, obj);
	}

	public void cd(PrintWriter writer, String cmd, String path) throws Exception {
		if (path.startsWith("/")) {
			clearPaths();
			path = path.substring(1);
		}
		for (String p : path.split("/")) {
			if (".".equals(p)) {
				continue;
			}
			if ("..".equals(p)) {
				cdParent(writer, cmd, path);
				continue;
			}
			boolean ret = cdNextRelative(writer, cmd, p);
			if (!ret) {
				return;
			} else {
				vars.put("_this", paths.getLast());
			}
				
		}
	}

	public void cdParent(PrintWriter writer, String cmd, String path) throws Exception {
		if (paths.size() == 1) {
			return;
		}
		pathNames.removeLast();
		paths.removeLast();
		vars.put("_this", paths.getLast());
	}

	public Object getByNextPath(PrintWriter writer, String cmd, String path, Object node) throws Exception {
		{
			Class cls = node.getClass();
			if (cls.getName().startsWith("java.")) {
				if (!Map.class.isAssignableFrom(cls)) {
					writer.println("-bash: getByNextPath : " + path + ": incompatible for primtive type :" + cls.getName());
					writer.flush();
					return null;
				}
			}
		}

		Object nextObj = null;
		String parts[] = path.split("\\$");
		if (Map.class.isAssignableFrom(node.getClass())) {
			Map map = (Map) node;
			nextObj = map.get(parts[0]);
		} else {
			Field field = ObjectUtil.getFieldByName(node.getClass(), parts[0]);
			field.setAccessible(true);
			nextObj = field.get(node);
		}

		if (nextObj == null) {
			writer.println("-bash: getByNextPath: " + parts[0] + ": No such file or directory");
			writer.flush();
			return null;
		}

		Class cls = nextObj.getClass();

		if (path.indexOf("$") > -1) {
			if (Map.class.isAssignableFrom(cls)) {
				Map map = (Map) nextObj;
				nextObj = map.get(parts[1]);
			} else if (List.class.isAssignableFrom(cls) || cls.isArray()) {
				int ind = Integer.valueOf(parts[1]);
				if (cls.isArray()) {
					int len = Array.getLength(nextObj);
					if (ind >= len) {
						writer.println("-bash: cd:" + parts[1] + ": Out of range in Array");
						writer.flush();
						return null;
					}
					nextObj = Array.get(nextObj, ind);
				} else {
					List list = (List) nextObj;
					if (ind >= list.size()) {
						writer.println("-bash: getByNextPath:" + parts[1] + ": Out of range in List");
						writer.flush();
						return null;
					}
					nextObj = list.get(ind);
				}
			} else {
				writer.println("-bash: getByNextPath: " + parts[1] + ": incompatible for cls " + cls);
				writer.flush();
				return null;
			}
		}

		if (nextObj == null) {
			writer.println("-bash: getByNextPath: " + parts[1] + ": No such file or directory");
			writer.flush();
			return null;
		}

		return nextObj;
	}

	public boolean cdNextRelative(PrintWriter writer, String cmd, String path) throws Exception {
		Object node = null;
		if (this.paths.isEmpty()) {
			throw new RuntimeException("invalid data");
		}
		node = paths.getLast();

		{
			Class cls = node.getClass();
			if (cls.getName().startsWith("java.")) {
				if (!Map.class.isAssignableFrom(cls)) {
					writer.println("-bash: cd: " + path + ": incompatible for primtive type :" + cls.getName());
					writer.flush();
					return false;
				}
			}
		}

		Object nextObj = null;
		String parts[] = path.split("\\$");
		if (Map.class.isAssignableFrom(node.getClass())) {
			Map map = (Map) node;
			nextObj = map.get(parts[0]);
		} else {
			Field field = ObjectUtil.getFieldByName(node.getClass(), parts[0]);
			if(field == null) {
				throw new RuntimeException("field not found for " + node.getClass() + " with fieldname " + parts[0]);
			}
			field.setAccessible(true);
			nextObj = field.get(node);
		}

		if (nextObj == null) {
			writer.println("-bash: cd: " + parts[0] + ": No such file or directory");
			writer.flush();
			return false;
		}

		Class cls = nextObj.getClass();

		if (path.indexOf("$") > -1) {
			if (Map.class.isAssignableFrom(cls)) {
				Map map = (Map) nextObj;
				nextObj = map.get(parts[1]);
			} else if (List.class.isAssignableFrom(cls) || cls.isArray()) {
				int ind = Integer.valueOf(parts[1]);
				if (cls.isArray()) {
					int len = Array.getLength(nextObj);
					if (ind >= len) {
						writer.println("-bash: cd:" + parts[1] + ": Out of range in Array");
						writer.flush();
						return false;
					}
					nextObj = Array.get(nextObj, ind);
				} else {
					List list = (List) nextObj;
					if (ind >= list.size()) {
						writer.println("-bash: cd:" + parts[1] + ": Out of range in List");
						writer.flush();
						return false;
					}
					nextObj = list.get(ind);
				}
			} else {
				writer.println("-bash: cd: " + parts[1] + ": incompatible for cls " + cls);
				writer.flush();
				return false;
			}
		}

		if (nextObj == null) {
			writer.println("-bash: cd: " + parts[1] + ": No such file or directory");
			writer.flush();
			return false;
		}

		cls = nextObj.getClass();
		if (List.class.isAssignableFrom(cls) || Map.class.isArray() || cls.isArray()) {
			writer.println("-bash: cd: " + path + ": cannot return List|Map|Array");
			writer.flush();
			return false;
		}

		paths.add(nextObj);
		pathNames.add(path);

		pwdCmd(writer, cmd);
		return true;
	}

	public void delCmd(PrintWriter writer, String cmd, String key) {
		vars.remove(key);
		writer.println("deleted");
	}

	public void newObj(PrintWriter writer, String cmd, String key, String... params) throws Exception {
		Class cls = getClass().getClassLoader().loadClass(params[0]);
		Object obj = cls.newInstance();
		vars.put(key, obj);
		writer.println("created");
		writer.println(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj));
	}

	public void load(PrintWriter writer, String cmd, String key, String file, String clsFullName) throws Exception {
		Object path = vars.get("_ctxPath");
		Class<? extends Object> cls = getClass().getClassLoader().loadClass(clsFullName);
		String content = IOUtil.getFileResourceAsString(path + "/" + file, "UTF8");
		Object obj = new Gson().fromJson(content, cls);
		vars.put(key, obj);
		writer.println("loaded");
		writer.println(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj));
	}

	public void dump(PrintWriter writer, String cmd, String key, String file) throws Exception {
		Object path = vars.get("_ctxPath");
		Object obj = vars.get(key);
		String content = new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj);
		IOUtil.writeFileContent(path + "/" + file, content);
		writer.println("dumped");
		writer.println(content);
	}

	public void listCmd(PrintWriter writer, String cmd) throws Exception {
		writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(ObjectUtil.deepToString(vars)));
	}
	
	public void msCmd(PrintWriter writer, String cmd) throws Exception {
		Object obj = vars.get("_this");
		if(obj == null) {
			writer.println("null obj");
			return;
		}
		List<Method> list = new ArrayList<Method>();
		ObjectUtil.getAllMethods(obj.getClass(), list);
		for(Method m : list) {
			writer.println(m.getName());
		}
	}
	
	public void reflect(PrintWriter writer, String method, String args[]) throws Exception {
		Object obj = vars.get("_this");
		if(obj == null) {
			writer.println("null obj");
			return;
		}
		
		Method m = ObjectUtil.getMethodByName(obj.getClass(), method);
		if(m == null) {
			writer.println("method not found");
			return;
		}
		
		Type[] ts = m.getParameterTypes();
		Object[] invokeArgs = null;
		if(ts == null) {
//			invokeArgs = 
		}
	}
	
	public void hkeys(PrintWriter writer, String cmd, String key) throws Exception {
		Object obj = getByEL(key);
		if (obj == null) {
			writer.println("null");
			return;
		}
		printKeys(writer, obj);
	}
	
	public void hset(PrintWriter writer, String cmd, String storeKey, String exp) throws Exception {
		Object obj = this.paths.getLast();
		if(obj == null) {
			writer.println("_this is null");
			return;
		}
		
		if(!Map.class.isAssignableFrom(obj.getClass())) {
			writer.println("_this is not map:" + obj.getClass().getName());
			return;
		}
		
		Map map = (Map)obj;
		System.out.println("find as :" + exp + " " + exp.getClass());
		for(Object k : map.keySet()) {
			System.out.println("find:" + k + " " + k.getClass());
			if(k.toString().equals(exp.toString())) {				
				Object finded = map.get(k);
				vars.put(storeKey, finded);
				writer.println("cannot find:" + exp);
				return;
			}
		}
		writer.println("cannot find:" + exp);
		return;
	}
	
	public void hdel(PrintWriter writer, String cmd, String key) throws Exception {
		Object obj = this.paths.getLast();
		if(obj == null) {
			writer.println("_this is null");
			return;
		}
		
		if(!Map.class.isAssignableFrom(obj.getClass())) {
			writer.println("_this is not map:" + obj.getClass().getName());
			return;
		}
		
		Map map = (Map)obj;

		Object findedKey = null;
		for(Object k : map.keySet()) {
			if(k.toString().equals(key.toString())) {				
				findedKey = k;
				break;
			}
		}
		if(findedKey != null) {
			writer.println("obj exists");
			map.remove(findedKey);
			writer.println("removed obj");
		} else {
			writer.println("obj not found ");
		}
		return;
	}

	private void printKeys(PrintWriter writer, Object obj) throws Exception {
		if (Map.class.isAssignableFrom(obj.getClass())) {
			writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(((Map) obj).keySet()));
			return;
		}

		List<Field> list = new ArrayList<Field>();
		ObjectUtil.getAllFields(obj.getClass(), list);
		List<String> list2 = new ArrayList<String>();
		for (Field f : list) {
			list2.add(f.getName() + "    " + f.getType().getSimpleName());
		}
		writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(list2));
	}

	public void keysCmd(PrintWriter writer, String cmd) {
		writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(vars.keySet()));
	}

	/**
	 * set matchSrv a
	 * 
	 * @param cmd
	 * @param key
	 * @param params
	 */
	public void setCmd(PrintWriter writer, String cmd, String key, String... params) {
		Object obj = executeObject(params[0], null);
		vars.put(key, obj);
		if (obj == null) {
			writer.println("set " + key + " null");
		} else {
			writer.println("set " + key + " " + obj.toString());
		}
	}

	public void elinvoke(PrintWriter writer, String cmd, String key, String express) throws Exception {
		Object obj = getByEL(key);
		if (obj == null) {
			writer.println("getbyel " + key + ", return null");
			return;
		}
		vars.put("_tmp", obj);
		express = "_tmp." + express;

		obj = executeObject(express, null);
		if (obj == null) {
			writer.println("invoke " + express + ", return null");
		} else {
			writer.println("invoke " + express + ", return " + new GsonBuilder().setPrettyPrinting().create().toJson(obj));
		}
	}

	public void invoke(PrintWriter writer, String cmd, String key) {
		Object obj = executeObject(key, null);
		if (obj == null) {
			writer.println("invoke " + key + ", return null");
		} else {
			writer.println("invoke " + key + ", return " + new GsonBuilder().setPrettyPrinting().create().toJson(obj));
		}
	}

	public void elset(PrintWriter writer, String cmd, String key, String... params) throws Exception {
		Object obj = getByEL(params[0]);
		vars.put(key, obj);
		if (obj == null) {
			writer.println("set " + key + " null");
		} else {
			writer.println("set " + key + " " + obj.toString());
		}
	}

	public void clearCmd(PrintWriter writer, String cmd, String key, String[] params) {
		init();
		writer.println("cleared");
	}

	private void clearVars() {
		vars.clear();
		binding.clearVariables();
	}

	private void clearPaths() {
		paths.clear();
		paths.add(vars);
		pathNames.clear();
		pathNames.add("/");
		vars.put("_this", paths.getLast());
	}

	public Object executeObject(String script, Map<String, Object> vars) {

		logger.debug("执行:" + script);
		setParameters(shell, this.vars);

		script = script.replace("&apos;", "'").replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<").replace("&nuot;", "\n").replace("&amp;", "&");

		Object rtn = shell.evaluate(script);
		binding.clearVariables();
		return rtn;

	}

	private void setParameters(GroovyShell shell, Map<String, Object> vars) {
		if (vars == null)
			return;
		Set<Map.Entry<String, Object>> set = vars.entrySet();
		for (Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
			shell.setVariable(entry.getKey(), entry.getValue());
			// shell.setProperty(property, newValue)
		}
	}	
}
