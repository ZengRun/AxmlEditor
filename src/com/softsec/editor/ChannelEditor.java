/**
 * 2014,12,10
 * 修改<application>中android:name值
 * 同时添加<meta-data>，android:name="APPLICATION_CLASS_NAME"
 * android:value设为原<application>中android:name的值(完整路径)
 * 若原<application>中无android:name属性，android:value="android.app.Application"
 */
package com.softsec.editor;

import java.util.List;

import com.softsec.editor.decode.AXMLDoc;
import com.softsec.editor.decode.BTagNode;
import com.softsec.editor.decode.BXMLNode;
import com.softsec.editor.decode.StringBlock;
import com.softsec.editor.decode.BTagNode.Attribute;
import com.softsec.editor.utils.TypedValue;

public class ChannelEditor {
	
	private String mChannelName = "APPLICATION_CLASS_NAME";
	private String Application_name_value = "com.wb.myclassloader.MyApplication";
	private String mChannelValue = "android.app.Application";
	
	private int namespace;
	private int meta_data;//<meta-data>
	private int tag_app;  //<application>
	private int attr_name1;//<application>   android:name
	private int attr_name2;//<meta-data>   android:name
	private int attr_value;//<meta-data>     android:value
	private int channel_name1;//  <application>   android:name 的值
	private int channel_name2;//<meta-data>     android:name的值
	private int channel_value ;//<meta-data>  android:value 的值
	
	private int attr_package;
	private boolean flag=false;//标记原文件中<application>是否含有android:name属性
	private int ori_name_value;
	private int package_name;
	private String Package_Name;
	private String Ori_Name;
	private String New_Name;
	
	private AXMLDoc doc;
	
	public ChannelEditor(AXMLDoc doc){
		this.doc = doc;
	}
	
	//get package name 
	private void getPackageName(StringBlock sb){
		attr_package=sb.putString("package");
		BTagNode manifest = (BTagNode) doc.getManifestNode();
		Attribute attr_of_manifest[]=manifest.getAttribute();
		for(int i=0;i<attr_of_manifest.length;i++){
			if(attr_of_manifest[i].mName==attr_package){
				package_name=attr_of_manifest[i].mValue;
				break;
			}	
		}
		Package_Name=sb.getStringFor(package_name);
		//System.out.println("*******************package_name="+Package_Name);
	}
	
	// add resource and get mapping ids for "meta-data"
	private void registStringBlock2(StringBlock sb){
		meta_data = sb.putString("meta-data");
		attr_name2 = sb.putString("name"); 
        	channel_name2 = sb.putString(mChannelName);
		channel_value= sb.addString(mChannelValue);//now we have a seat in StringBlock
	}
	
	//add resource and get mapping ids for <application> "android:name"
	private void registStringBlock1(StringBlock sb){
		namespace = sb.putString("http://schemas.android.com/apk/res/android");
		attr_name1 = sb.putString("name");
		attr_value = sb.putString("value");
		tag_app = sb.putString("application");
		channel_name1 = sb.putString(Application_name_value);
	}
 	
   //add a tag:meta-data
	private void editNode2(AXMLDoc doc){
		BXMLNode application = doc.getApplicationNode(); //manifest node
		BTagNode softsec_meta = null;
		Attribute name_attr = new Attribute(namespace, attr_name2, TypedValue.TYPE_STRING);
		name_attr.setString( channel_name2 );
		Attribute value_attr = new Attribute(namespace, attr_value, TypedValue.TYPE_STRING);
		if(flag) //原来application标签中就有android:name属性，则meta-data 的value属性值为原application中name的属性值
			value_attr.setString( ori_name_value );			
		else //否则采用默认的属性值
		    value_attr.setString( channel_value );			
		softsec_meta = new BTagNode(-1, meta_data);//-1即0xFFFFFFFF
		
		if(!doc.getResBlock().mRawResIds.contains(0x01010003)){
			doc.getResBlock().mRawResIds.add(0x01010003);
			doc.getResBlock().prepare();
		}
		softsec_meta.setAttribute(name_attr);
		if(!doc.getResBlock().mRawResIds.contains(0x01010024)){
			doc.getResBlock().mRawResIds.add(0x01010024);
			doc.getResBlock().prepare();
		}
		softsec_meta.setAttribute(value_attr);
		
		softsec_meta.prepare();
		application.prepare();
		application.addChild(softsec_meta);
			
	}

	//change the value of android:name in tag <application>. And reserve the original value of android:name,in order to use in tag <meta-data>
	private void editNode1(AXMLDoc doc,StringBlock sb){
		BXMLNode manifest = doc.getManifestNode(); //manifest node
		List<BXMLNode> children = manifest.getChildren();
		BTagNode softsec_application = null;	
		end:for(BXMLNode node : children){
		BTagNode m = (BTagNode)node;
		if(tag_app == m.getName()) {
				softsec_application = m;
					break end;
			}
		}	
		if(softsec_application != null){
			Attribute attr[]=softsec_application.getAttribute();
			for(int i=0;i<attr.length;i++){
				if(attr[i].mName==attr_name1){ //if there is the attribute "android:name" in tag <application>
					ori_name_value=attr[i].mValue;
					Ori_Name=sb.getStringFor(ori_name_value);//reserve the value of android:name
					//System.out.println("*******************ori_name="+Ori_Name);
					if(Ori_Name.startsWith(".")) 
						New_Name=Package_Name+Ori_Name;//如果原始name属性值以.开头，则加上包名，将其完整路径存入New_Name。
					else
						New_Name=Ori_Name;
					//System.out.println("********************new_name="+New_Name);
					ori_name_value=sb.putString(New_Name);
					attr[i].setValue(TypedValue.TYPE_STRING, channel_name1);//change the value of attribute android:name
					flag=true;
					break;
				}
			}
		if(!flag){//if there isn't the attribute "android:name" in tag <application>
			Attribute name_attr = new Attribute(namespace, attr_name1, TypedValue.TYPE_STRING);
			name_attr.setString( channel_name1 );
			softsec_application.setAttribute(name_attr);//add the attribute "android:name"	
			}
		}
}

	public void commit() {
		getPackageName(doc.getStringBlock());
		registStringBlock1(doc.getStringBlock());
		editNode1(doc,doc.getStringBlock());
		registStringBlock2(doc.getStringBlock());
		editNode2(doc);	
	}
}
