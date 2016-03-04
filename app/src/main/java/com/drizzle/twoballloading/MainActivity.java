package com.drizzle.twoballloading;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.drizzle.loadingview.TwoBallLoadingView;

public class MainActivity extends AppCompatActivity {
	private TwoBallLoadingView mTwoBallLoadingView;
	private Button startButton;
	private Button stopButton;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTwoBallLoadingView = (TwoBallLoadingView)findViewById(R.id.loading_view);
		startButton = (Button)findViewById(R.id.button);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				mTwoBallLoadingView.startLoading();
			}
		});
		stopButton=(Button)findViewById(R.id.stop);
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				mTwoBallLoadingView.stop();
			}
		});
	}
}
