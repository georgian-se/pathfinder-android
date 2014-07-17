package gse.pathfinder;

import gse.pathfinder.api.NetworkUtils;
import gse.pathfinder.models.Tower;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TowerDialog extends DialogFragment {
	private Tower tower;
	private TextView txtRegion;
	private TextView txtLinename;
	private TextView txtName;
	private ProgressBar prgDownload;
	private ImageView imgTower;
	private TextView imgCount;
	private List<String> files;
	private int currImage;

	public TowerDialog(Tower tower) {
		this.tower = tower;
	}

	@SuppressLint("InflateParams")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View view = inflater.inflate(R.layout.fragment_tower, null);

		txtName = (TextView) view.findViewById(R.id.name_tower_fragment);
		txtLinename = (TextView) view.findViewById(R.id.linename_tower_fragment);
		txtRegion = (TextView) view.findViewById(R.id.region_tower_fragment);
		prgDownload = (ProgressBar) view.findViewById(R.id.progress_tower_fragment);
		imgTower = (ImageView) view.findViewById(R.id.image_view_tower_fragment);
		imgCount = (TextView) view.findViewById(R.id.image_count_tower_fragment);

		builder.setView(view);
		builder.setTitle("ანძის თვისებები");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				TowerDialog.this.dismiss();
			}
		});

		GestureDetector gdt = new GestureDetector(getActivity(), new SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

				Log.d("FLING", "X: " + velocityX + "; Y: " + velocityY);

				return super.onFling(e1, e2, velocityX, velocityY);
			}
		});

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		txtName.setText("#" + tower.getName());
		txtLinename.setText(tower.getLinename());
		txtRegion.setText(tower.getRegion());

		imgCount.setVisibility(View.INVISIBLE);
		imgTower.setVisibility(View.INVISIBLE);
		prgDownload.setVisibility(View.VISIBLE);

		new ImageDownload().execute(tower.getImages().toArray(new String[] {}));
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (files != null) {
			for (String f : files) {
				new File(f).delete();
			}
			files.clear();
		}
	}

	private void displayImages(List<String> images) {
		this.files = images;
		prgDownload.setVisibility(View.INVISIBLE);
		if (!images.isEmpty()) {
			imgCount.setVisibility(View.VISIBLE);
			imgTower.setVisibility(View.VISIBLE);
			showImage(0);
		}
	}

	private void showImage(int index) {
		this.currImage = index;
		Bitmap myBitmap = BitmapFactory.decodeFile(files.get(index));
		imgTower.setImageBitmap(myBitmap);
		imgCount.setText((currImage + 1) + " / " + files.size());
	}

	private class ImageDownload extends AsyncTask<String, Integer, List<String>> {
		@Override
		protected List<String> doInBackground(String... params) {
			List<String> files = new ArrayList<String>();
			for (String url : params) {
				try {
					files.add(NetworkUtils.downloadFile(TowerDialog.this.getActivity(), url));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return files;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			super.onPostExecute(result);
			displayImages(result);
		}
	};
}
