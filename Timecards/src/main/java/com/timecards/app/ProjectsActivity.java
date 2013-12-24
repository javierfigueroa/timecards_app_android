package com.timecards.app;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.timecards.R;
import com.timecards.api.Service;
import com.timecards.api.model.Project;
import com.timecards.api.model.Timecard;
import com.timecards.libs.ProgressHUD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProjectsActivity extends ListActivity implements DialogInterface.OnCancelListener{

    public static final String TAG = ProjectsActivity.class.getSimpleName();

    private ProgressHUD mProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);


        List<Map<String, Object>> projects = getProjects();
        setListAdapter(new SimpleAdapter(this, projects,
                R.layout.project_list_item, new String[] { "title" },
                new int[] { R.id.text1 }));
        getListView().setSelector(R.drawable.abc_list_selector_holo_light);
        getListView().setTextFilterEnabled(true);
    }


    protected List<Map<String, Object>> getProjects() {
        List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();
        for (Project project : ActionsActivity.mProjects) {
            addItem(myData, project, project.getName());
        }

        return myData;
    }

    protected void addItem(List<Map<String, Object>> data, Project project, String name) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("data", project);
        data.add(temp);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, Object> map = (Map<String, Object>)l.getItemAtPosition(position);

        Timecard timecard = CurrentTimecard.getCurrentTimecard();
        Project project = (Project) map.get("data");
        //update project
        Service.setProject(this, timecard, project, new Callback<Timecard>() {
            @Override
            public void success(Timecard timecard, Response response) {
                Log.v(TAG, response.toString());
                CurrentTimecard.setCurrentTimecard(timecard);
                showProgress(false);
                finish();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.v(TAG, retrofitError.toString());
                showProgress(false);
            }
        });

        showProgress(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.projects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (show) {
            mProgressHUD = ProgressHUD.show(ProjectsActivity.this, getString(R.string.loading), true, true, this);
        }else{
            mProgressHUD.dismiss();
        }
    }

    @Override
     public void onCancel(DialogInterface dialogInterface) {
        mProgressHUD.dismiss();
    }
}
