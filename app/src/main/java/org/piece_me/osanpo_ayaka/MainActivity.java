package org.piece_me.osanpo_ayaka;
import android.content.res.*;
import android.graphics.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.*;
import android.view.*;

import java.util.concurrent.*;
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Runnable {
	private final int DISP_X = 128;
	private final int DISP_Y = 88;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private Paint paint;
	private Bitmap[] bitmap_a = new Bitmap[5];
	private Bitmap bitmap_b;
	private ScheduledExecutorService executor;
	private ScheduledFuture<?> future;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//サーフェイスビューを取得する。
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
		if(surfaceView == null) { throw new RuntimeException(); }        //レイアウト定義のバグ。
		//サーフェイスホルダーのコールバックを登録する。
		surfaceView.getHolder().addCallback(this);
		//グラフィックスコンテキストを作成する。
		paint = new Paint();
		//リソースを読み込む。
		{
			Resources resources = getResources();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			bitmap_a[0] = BitmapFactory.decodeResource(resources, R.drawable.a0, options);
			bitmap_a[1] = BitmapFactory.decodeResource(resources, R.drawable.a1, options);
			bitmap_a[2] = BitmapFactory.decodeResource(resources, R.drawable.a2, options);
			bitmap_a[3] = BitmapFactory.decodeResource(resources, R.drawable.a3, options);
			bitmap_a[4] = BitmapFactory.decodeResource(resources, R.drawable.a4, options);
			bitmap_b = BitmapFactory.decodeResource(resources, R.drawable.b, options);
		}
		//スケジューラーを作成する。
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	@Override
	protected void onResume() {
		super.onResume();
		//スケジューリングを開始する。
		future = executor.scheduleAtFixedRate(this, 0, 80, TimeUnit.MILLISECONDS);
	}
	@Override
	protected void onPause() {
		super.onPause();
		//スケジューリングを停止する。
		future.cancel(false);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//スケジューラーを終了する。
		executor.shutdown();
	}
	//----------------------------------------------------------------------
	//	SurfaceHolder.Callback
	//----------------------------------------------------------------------
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.holder = holder;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		/** no job **/
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.holder = null;
	}
	//----------------------------------------------------------------------
	//	Runnable
	//----------------------------------------------------------------------
	private int bg_x;	//背景の座標
	private int anim;	//キャラのアニメーションカウンタ
	private int[/*anim*/] ptn = {0,1,2,0,3,4};	//キャラのアニメーションパターン
	@Override
	public void run() {
		if(holder == null) { return; }
		{
			Canvas canvas = holder.lockCanvas();
			canvas.save();
			//スケーリング
			{
				int w = canvas.getWidth();
				int h = canvas.getHeight();
				float sx = (float)w / DISP_X;
				float sy = (float)h / DISP_Y;
				float s = Math.max(sx, sy);
				canvas.translate(
					(w - (DISP_X * s)) / 2,
					(h - (DISP_Y * s)) / 2);
				canvas.scale(s, s);
			}
			//背景
			{
				canvas.drawBitmap(bitmap_b, bg_x, 0, null);
				canvas.drawBitmap(bitmap_b, bg_x + DISP_X, 0, null);
				if((bg_x -= 4) <= -DISP_X) { bg_x += DISP_X; }
			}
			//キャラ
			{
				canvas.drawBitmap(bitmap_a[ptn[anim]], 40, 0, null);
				if(++anim >= ptn.length) { anim = 0; }
			}
			canvas.restore();
			holder.unlockCanvasAndPost(canvas);
		}
	}
}
