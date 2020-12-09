package com.example.phong;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class Sphere {
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer; 
	private FloatBuffer normalBuffer; 
	private ShortBuffer drawListBuffer;
	
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle, mNormalHandle;
	private int mMVPMatrixHandle, mNormalMatHandle;
	
	static final int COORDS_PER_VERTEX = 3;
	
	private float [] vertices; 
	private float [] normals; 
	static float [] colors; 
	
	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };
	
	private int vertexCount; 
	private final int vertexStride = COORDS_PER_VERTEX * 4;
	
	float lightDir[] = {-1.0f, 1.0f, 8.0f};
	
	private void createSphere(double r, int lats, int longs) {
		        int i, j;
		        
		        vertices = new float[lats*longs*6*3];
		        normals = new float[lats*longs*6*3]; 
		        colors = new float[lats*longs*6*3]; 
		        
		        vertexCount = vertices.length / COORDS_PER_VERTEX;
		        int triIndex = 0; 
		        for(i = 0; i < lats; i++) {
		            double lat0 = Math.PI * (-0.5 + (double) (i) / lats);
		           double z0  = Math.sin(lat0);
		           double zr0 =  Math.cos(lat0);
		    
		           double lat1 = Math.PI * (-0.5 + (double) (i+1) / lats);
		           double z1 = Math.sin(lat1);
		           double zr1 = Math.cos(lat1);
		 
		           for(j = 0; j < longs; j++) {
		               double lng = 2 * Math.PI * (double) (j - 1) / longs;
		               double x = Math.cos(lng);
		               double y = Math.sin(lng);
		    
		               
		               lng = 2 * Math.PI * (double) (j) / longs;
		               double x1 = Math.cos(lng);
		               double y1 = Math.sin(lng);
		               
				vertices[triIndex*9 + 0 ] = (float)(x * zr0); vertices[triIndex*9 + 1 ] = (float)(y * zr0); vertices[triIndex*9 + 2 ] = (float) z0;
				vertices[triIndex*9 + 3 ] = (float)(x * zr1); vertices[triIndex*9 + 4 ] = (float)(y * zr1); vertices[triIndex*9 + 5 ] = (float) z1;
				vertices[triIndex*9 + 6 ] = (float)(x1 * zr0); vertices[triIndex*9 + 7 ] = (float)(y1 * zr0); vertices[triIndex*9 + 8 ] = (float) z0;
		               
		               triIndex ++; 
		               vertices[triIndex*9 + 0 ] = (float)(x1 * zr0);   vertices[triIndex*9 + 1 ] = (float)(y1 * zr0);  	vertices[triIndex*9 + 2 ] = (float) z0;
		               vertices[triIndex*9 + 3 ] = (float)(x * zr1);    vertices[triIndex*9 + 4 ] = (float)(y * zr1);   	vertices[triIndex*9 + 5 ] = (float) z1;  
		               vertices[triIndex*9 + 6 ] = (float)(x1 * zr1);    vertices[triIndex*9 + 7 ] = (float)(y1 * zr1); 	vertices[triIndex*9 + 8 ] = (float) z1;
		               
		               for (int kk = -9; kk<9 ; kk++) {
		            	   normals[triIndex*9 + kk] = vertices[triIndex*9+kk];
		            	   if((triIndex*9 + kk)%3 == 2)
		            		   colors[triIndex*9 + kk] = 1;
		            	   else
		            		   colors[triIndex*9 + kk] = 0;
		               }		               
		               triIndex ++; 
		           }
		       }
	}
	
	public Sphere(double r, int lats, int longs) {
		
		createSphere(r, lats, longs); 
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		
		ByteBuffer bb2 = ByteBuffer.allocateDirect(colors.length * 4);
		bb2.order(ByteOrder.nativeOrder());
		colorBuffer = bb2.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

		ByteBuffer bb3 = ByteBuffer.allocateDirect(normals.length * 4);
		bb3.order(ByteOrder.nativeOrder());
				
		normalBuffer = bb3.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.position(0);
				
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);

		mProgram = MyGL20Renderer.createShaderProgram();
	}
	
	
	
	public void draw(float[] mvpMatrix, float [] normalMat, float [] mvMat) {
		
		GLES20.glUseProgram(mProgram);
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		
		int light = GLES20.glGetUniformLocation(mProgram, "lightDir");
		GLES20.glUniform3fv(light, 1, lightDir, 0);
		mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
		
		GLES20.glEnableVertexAttribArray(mColorHandle);
		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, colorBuffer);
		
		mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");	
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, normalBuffer);
		
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		mNormalMatHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMat");
		
		int MVMatHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
		
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GLES20.glUniformMatrix4fv(mNormalMatHandle, 1, false, normalMat, 0);
		GLES20.glUniformMatrix4fv(MVMatHandle, 1, false, mvMat, 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
		GLES20.glDisableVertexAttribArray(mNormalHandle);
	}
}

