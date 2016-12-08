package com.softsec.editor.decode;

import java.io.IOException;
import java.util.ArrayList;

public class ResBlock implements IAXMLSerialize{
	private static final int TAG = 0x00080180;
	
	private int mChunkSize;
	private int[] mRawResId;
	public ArrayList<Integer> mRawResIds=new ArrayList<Integer>();
	
	public void print(){
		StringBuilder sb = new StringBuilder();
		
		for(int id : getResourceIds()){
			sb.append(id);
			sb.append(" ");
		}
		
		System.out.println(sb.toString());
	}
	
	public void read(IntReader reader) throws IOException{
		mChunkSize = reader.readInt();
		
		if(mChunkSize < 8 || (mChunkSize % 4)!= 0){
			throw new IOException("Invalid resource ids size ("+mChunkSize+").");
		}
		
		mRawResId = reader.readIntArray(mChunkSize/4 - 2);//subtract base offset (type + size)
		for(int i=0;i<mRawResId.length;i++){  
			mRawResIds.add(mRawResId[i]);  
        }  
	}
	
	private final int INT_SIZE = 4;
	public void prepare(){
		int base = 2*INT_SIZE;
		int resSize = mRawResIds == null ? 0:mRawResIds.size()*INT_SIZE;
		mChunkSize = base + resSize;
	}
	
	public void write(IntWriter writer) throws IOException {
		writer.writeInt(TAG);
		writer.writeInt(mChunkSize);
		
		if(mRawResIds != null){
			for(int id : mRawResIds){
				writer.writeInt(id);
			}
		}
	}
	
	public ArrayList<Integer> getResourceIds(){
		return mRawResIds;
	}
	
	public int getResourceIdAt(int index){
		return mRawResIds.get(index);
	}

	public int getSize() {
		return mChunkSize;
	}

	public int getType() {
		return TAG;
	}

	public void setSize(int size) {
		// TODO Auto-generated method stub		
	}

	public void setType(int type) {
		// TODO Auto-generated method stub		
	}
}
