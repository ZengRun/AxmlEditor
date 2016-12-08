package com.softsec.editor;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.softsec.editor.decode.AXMLDoc;

public class Main {
	public static void main(String[] args) {
		try{
			AXMLDoc doc = new AXMLDoc();
			doc.parse(new FileInputStream(args[0]));
//			doc.testSize();
//			doc.print();
//			for(int i=0;i<doc.getResBlock().getResourceIds().size();i++)//测试代码 
//				System.out.println(i+": "+doc.getResBlock().getResourceIdAt(i));
			
			ChannelEditor editor = new ChannelEditor(doc);
			editor.commit();
			doc.build(new FileOutputStream(args[1]));

//			AXMLDoc doc2 = new AXMLDoc();
//			doc2.parse(new FileInputStream(args[1]));
//			doc2.testSize();
//			doc2.print();
//		for(int i=0;i<doc2.getResBlock().getResourceIds().size();i++)//测试代码 
//				System.out.println(i+": "+doc2.getResBlock().getResourceIdAt(i));

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
