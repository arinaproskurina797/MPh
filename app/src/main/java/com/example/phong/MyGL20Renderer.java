package com.example.phong;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glUseProgram;

public class MyGL20Renderer implements GLSurfaceView.Renderer {

	private static Context context;
	public volatile float mXAngle;
	public volatile float mYAngle;
	public volatile float mZoom;

	private final static long TIME = 10000;
	private final float[] mVMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mNormalMatrix = new float[16]; 
	private final float[] mMVPMatrix = new float[16];
	private final float[] mRotationMatrixX = new float[16];
	private final float[] mRotationMatrixY = new float[16];
	private final float[] mPVMatrix = new float [16];
	private final float[] mTempMatrix = new float[16];
	private final float[] mMVMatrix = new float[16];

	private Sphere[] sphere;

	public MyGL20Renderer(Context context) {
		this.context = context;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig cfg) {
		
		GLES20.glClearColor(222.0f / 255.0f, 222.0f / 255.0f, 222.0f / 255.0f, 1.0f);
		GLES20.glEnable(GL10.GL_DEPTH_TEST);
	    GLES20.glDepthFunc(GL10.GL_LEQUAL);
	    
	    mZoom = -6f;

	    sphere = new Sphere[4];
	    for(int i = 0; i < 4; i++)
	    	sphere[i] = new Sphere(5, 30, 60);
		//sphere = new Sphere(10, 30, 60);

	}
	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
		
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, mZoom, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		//createViewMatrix();

		Matrix.multiplyMM(mPVMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

		Matrix.setRotateM(mRotationMatrixX, 0, mXAngle, 0, 1.0f, 0f);

		Matrix.setRotateM(mRotationMatrixY, 0, mYAngle, 1.0f, 0, 0);

		Matrix.multiplyMM(mTempMatrix, 0, mPVMatrix, 0, mRotationMatrixX, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);

		Matrix.multiplyMM(mTempMatrix, 0, mVMatrix, 0, mRotationMatrixX, 0);
		Matrix.multiplyMM(mMVMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);

		Matrix.invertM(mTempMatrix, 0, mMVMatrix, 0);
		Matrix.transposeM(mNormalMatrix, 0, mTempMatrix, 0);
		drawBalls();
	}	
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
	}

	public static int createShaderProgram() {
		int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
		int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
		int programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
		return programId;
	}
		
	private void drawBalls() {
		float[] scalerMatrix = new float[16];
		float[] finalMVPMatrix = new float[16];
		float[] tempMatrix = new float[16];

		createViewMatrix();
		setModelMatrix();
		Matrix.setIdentityM(scalerMatrix, 0);
		Matrix.scaleM(scalerMatrix, 0, 1.2f, 1.2f, 0.5f);
		Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);

		Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 0.3f, -2f, 8f);
		sphere[0].draw(finalMVPMatrix, mNormalMatrix, mVMatrix);
		setModelMatrix();

		Matrix.setIdentityM(scalerMatrix, 0);
		Matrix.scaleM(scalerMatrix, 0, 1.5f, 1.5f, 0.5f);
		Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);

		Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -1.7f, 0f, 5f);
		sphere[1].draw(finalMVPMatrix, mNormalMatrix, mVMatrix);
		setModelMatrix();

		Matrix.setIdentityM(scalerMatrix, 0);
		Matrix.scaleM(scalerMatrix, 0, 2.5f, 2.5f, 0.5f);
		Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);

		Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, 0.7f, .5f, 5f);
		sphere[2].draw(finalMVPMatrix, mNormalMatrix, mVMatrix);

		Matrix.setIdentityM(scalerMatrix, 0);
		Matrix.scaleM(scalerMatrix, 0, .7f, .7f, 0.5f);
		Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);

		Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -1.5f, 4.5f, 5f);
		setModelMatrix();
		sphere[3].draw(finalMVPMatrix, mNormalMatrix, mVMatrix);
	}

	private void setModelMatrix() {
		//float angle = (float)(SystemClock.uptimeMillis() % TIME) / TIME * 360;
		//Matrix.rotateM(mNormalMatrix, 0, angle, 0, 1, 1);
	}

	private void createViewMatrix() {

		float time = (float)(SystemClock.uptimeMillis() % TIME) / TIME;
		float angle = time  *  2 * 3.1415926f;

		float eyeX = (float) (Math.cos(angle) * 4f);
		float eyeY = 0f;
		float eyeZ = -6f;

		eyeX = (float) (Math.cos(angle) * 4f);
		eyeY = 1f;
		eyeZ = (float) (Math.sin(angle) * 4f);

		float centerX = 0;
		float centerY = 0;
		float centerZ = 0;

		float upX = 0;
		float upY = 1;
		float upZ = 0;

		Matrix.setLookAtM(mVMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
	}
}
