package net.kwami.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class MappedObject {

	public void populateFields(ResultSet rs) throws Exception {
		ResultSetMetaData meta = rs.getMetaData();
		Map<String, Integer> colnameIndex = new HashMap<>();
		for (int i = 0; i <= meta.getColumnCount(); i++) {
			colnameIndex.put(meta.getColumnName(i), i);
		}
		for (Field f : this.getClass().getDeclaredFields()) {
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers))
				continue;
			Class<?> fldClass = f.getType();
			String colName = f.getName().replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
			if (fldClass.isPrimitive()) {
				if (fldClass.getName().equals("boolean")) {
					f.set(this, rs.getBoolean(colName));
					continue;
				}
				if (fldClass.getName().equals("byte")) {
					f.set(this, rs.getByte(colName));
					continue;
				}
				if (fldClass.getName().equals("char")) {
					f.set(this, rs.getByte(colName));
					continue;
				}
				if (fldClass.getName().equals("short")) {
					f.set(this, rs.getShort(colName));
					continue;
				}
				if (fldClass.getName().equals("int")) {
					f.set(this, rs.getInt(colName));
					continue;
				}
				if (fldClass.getName().equals("long")) {
					f.set(this, rs.getLong(colName));
					continue;
				}
				if (fldClass.getName().equals("float")) {
					f.set(this, rs.getFloat(colName));
					continue;
				}
				if (fldClass.getName().equals("double")) {
					f.set(this, rs.getDouble(colName));
					continue;
				}
				continue;
			}
			if (fldClass.getSimpleName().equals("String")) {
				f.set(this, rs.getString(colName));
				continue;
			}
			if (fldClass.getName().equals("java.util.Date")) {
				Integer colIndex = colnameIndex.get(fldClass.getName());
				if (colIndex == null)
					continue;
				if (meta.getColumnType(colIndex) == Types.TIMESTAMP) {
					f.set(this, new Date(rs.getTimestamp(colName).getTime()));
					continue;
				}
				if (meta.getColumnType(colIndex) == Types.DATE) {
					f.set(this, new Date(rs.getDate(colName).getTime()));
					continue;
				}
				if (meta.getColumnType(colIndex) == Types.TIME) {
					f.set(this, new Date(rs.getTime(colName).getTime()));
					continue;
				}
				continue;
			}
			if (fldClass.getName().equals("[B")) { // byte[]
				Integer colIndex = colnameIndex.get(fldClass.getName());
				if (colIndex == null)
					continue;
				if (meta.getColumnType(colIndex) == Types.BINARY || meta.getColumnType(colIndex) == Types.VARBINARY
						|| meta.getColumnType(colIndex) == Types.LONGVARBINARY) {
					InputStream is = rs.getBinaryStream(colIndex);
					ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
					byte[] buf = new byte[256];
					int readCnt = 0;
					while ((readCnt = is.read(buf)) > 0) {
						baos.write(buf, 0, readCnt);
					}
					baos.flush();
					f.set(this, baos.toByteArray());
					continue;
				}

			}
			if (fldClass.getName().equals("[C")) { // char[]

			}
		}
	}

	public void copyFields(Object from) {
		Class<?> fromClass = from.getClass();
		for (Field f : this.getClass().getDeclaredFields()) {
//			Class<?> fldClass = f.getType();
//			System.out.printf("name=%s,class=%s\n", f.getName(), fldClass.getName());
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers))
				continue;
			try {
				Field fromField = fromClass.getDeclaredField(f.getName());
				f.set(this, fromField.get(from));
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}
}
