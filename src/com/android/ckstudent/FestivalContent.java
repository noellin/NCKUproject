package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This activity is to show activity details included name, date, place, detail content and poster.
 */
public class FestivalContent extends Activity {
	Menu menu;
	int mShortAnimationDuration;
	Animator mCurrentAnimator;
	ArrayList<Festival> festivals;
	TextView textView1, textView2, textView3, textView4;
	ImageView imageView;
	String url;
	Bitmap bitmap;
	String festivalName, festivalId;
	String festivalDate;
	String picURL;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.festival_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		festivals = new ArrayList<Festival>();
		textView1 = (TextView)this.findViewById(R.id.textView5);
		textView2 = (TextView)this.findViewById(R.id.textView6);
		textView3 = (TextView)this.findViewById(R.id.textView7);
		textView4 = (TextView)this.findViewById(R.id.textView4);
		imageView = (ImageView)this.findViewById(R.id.imageView1);
		mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImage();
            }
        });
		//Get information from FestivalFragment.
		Bundle bundle = this.getIntent().getExtras();
        festivalName = bundle.getString("festivalName");
        festivalDate = bundle.getString("festivalDate");
        festivalId = bundle.getString("festivalId");
        url = bundle.getString("Url");
		getFestival.execute();
	}
	//If can add to favorite.
	void addFavor() {
		//Create a festival object.
		Festival festival = new Festival();
		festival.name = festivalName;
		festival.date = festivalDate;
		festival.id = festivalId;
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not. If exists read it first.
		if (fileExistance("festival")) {
			FileInputStream fileInput;
			try {
				fileInput = openFileInput("festival");
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert into JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//If not exists, initial the JSONArray.
			jsonArray = new JSONArray();
		}
		try {
			//Add object to the end of the JSONArray.
			jsonObject.put("festivalName", festival.name);
			jsonObject.put("festivalDate", festival.date);
			jsonObject.put("festivalId", festival.id);
			jsonArray.put(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Write the change into file.
		FileOutputStream fileOutput;
		try {
			fileOutput = openFileOutput("festival", Context.MODE_PRIVATE);
			fileOutput.write(jsonArray.toString().getBytes());
			fileOutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//Notify.
		Toast.makeText(FestivalContent.this, "收藏成功", Toast.LENGTH_SHORT).show();
	}
	
	//Already added into favorite and would like to cancel it.
	void removeFavor() {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject= new JSONObject();
		int index = 0;
		FileInputStream fileInput;
		try {
			//Open file.
			fileInput = openFileInput("festival");
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			//Convert into JSONArray.
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				Festival festival = new Festival();
				//Convert into JSONObject.
				festival.name = jsonObject.getString("festivalName");
				festival.date = jsonObject.getString("festivalDate");
				festival.id = jsonObject.getString("festivalId");
				//Check the position of the activity.
				if (festivalName.equals(festival.name)) {
					index = i;
				}
				festivals.add(festival);
			}
			//Use the matche index to remove object.
			festivals.remove(index);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Initialize the JSONArray.
		jsonArray = new JSONArray();
		//If the arraysize isn't zero then write the entired data back.
		if (festivals.size() != 0) {
			//Convert into JSONArray.
			for (int i=0; i<festivals.size(); i++) {
				try {
					jsonObject.put("festivalName", festivals.get(i).name);
					jsonObject.put("festivalDate", festivals.get(i).date);
					jsonObject.put("festivalId", festivals.get(i).id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonArray.put(jsonObject);
			}
			FileOutputStream fileOutput;
			try {
				fileOutput = openFileOutput("festival", Context.MODE_PRIVATE);
				fileOutput.write(jsonArray.toString().getBytes());
				fileOutput.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//If the deleted object is the only object in the list, delete the file.
			File file = getBaseContext().getFileStreamPath("festival");
			file.delete();
		}
    	//Notify.
		Toast.makeText(FestivalContent.this, "取消收藏", Toast.LENGTH_SHORT).show();
	}
	
	//Get activity data by WebApi.
	AsyncTask<Void, Void, Festival> getFestival = new AsyncTask<Void, Void, Festival>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(FestivalContent.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Festival doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "ActivityInformation/" + festivalId);
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				//Process the response.
			    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			        HttpEntity httpEntity = httpResponse.getEntity();
			        InputStream content = httpEntity.getContent();			        
			        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			        StringBuilder builder = new StringBuilder();
			        String line;
			        while ((line = reader.readLine()) != null) {
			        	builder.append(line);
			        }
			        JSONArray jsonArray = new JSONArray(builder.toString());
			        //Convert into JSONObject.
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					Festival festival = new Festival();
					festival.id = jsonObject.getString("Id");
					festival.picUrl = jsonObject.getString("Picture");
					festival.name = jsonObject.getString("Name");
					festival.date = jsonObject.getString("Date");
					festival.place = jsonObject.getString("Place");
					festival.content = jsonObject.getString("Description");
					return festival;
			    }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Festival festival) {
			super.onPostExecute(festival);
			textView1.setText(festival.name);
			textView2.setText(festival.date);
			textView3.setText(festival.place);
			textView4.setText(festival.content);
			//If have picture or poster then get it.
			if (!"null".equals(festival.picUrl)) {
				//Turn to bitmap and set in imageView.
				picURL = festival.picUrl;
				byte[] buffer = Base64.decode(picURL, Base64.DEFAULT);
				bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
				imageView.setImageBitmap(bitmap);
				imageView.setPadding(10, 10, 10, 10);
				imageView.getLayoutParams().height = 320;
				imageView.getLayoutParams().width = 320;
			} else {
				//Set image Size.
				imageView.getLayoutParams().height = 0;
				imageView.getLayoutParams().width = 0;
				imageView.setVisibility(View.GONE);
			}
			mDialog.dismiss();
		}
	};
	
	//Check file exists or not.
	private boolean fileExistance(String fname) {
		File file = getBaseContext().getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		}
		else {
			return false;
		}    
	}
	
	//Check activity added to favorite or not.
	private boolean checkFavorable(String festivalName) {
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not.
		if (fileExistance("festival")) {
			FileInputStream fileInput;
			try {
				fileInput = openFileInput("festival");
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert into JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Convert into JSONObject.
			for (int i=0; i<jsonArray.length(); i++) {
				try {
					jsonObject = jsonArray.getJSONObject(i);
					//Find if there is a same activity.
					if (festivalName.equals(jsonObject.getString("festivalName"))) {
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			return false;
		}
		return false;
	}
	
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			removeFavor();
		}
	};
	
	void zoomImage() {
		// If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(bitmap);

        // Calculate the starting and ending bounds for the zoomed-in image. This step
        // involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail, and the
        // final bounds are the global visible rectangle of the container view. Also
        // set the container view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        imageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final bounds using the
        // "center crop" technique. This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }
        // Hide the thumbnail and show the zoomed-in view. When the animation begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        imageView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
        // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }
                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        imageView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		if (checkFavorable(festivalName)) {
			getMenuInflater().inflate(R.menu.nofavor, menu);
        } else {
        	getMenuInflater().inflate(R.menu.favor, menu);
        }
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.favor:
			addFavor();
			this.menu.clear();
			this.onCreateOptionsMenu(this.menu);
			break;
		case R.id.nofavor:
			removeFavor();
			this.menu.clear();
			this.onCreateOptionsMenu(this.menu);
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
