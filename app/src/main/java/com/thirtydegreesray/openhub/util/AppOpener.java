/*
 *    Copyright 2017 ThirtyDegreesRay
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.thirtydegreesray.openhub.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.ui.activity.IssueDetailActivity;
import com.thirtydegreesray.openhub.ui.activity.ProfileActivity;
import com.thirtydegreesray.openhub.ui.activity.RepositoryActivity;
import com.thirtydegreesray.openhub.ui.activity.ViewerActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by ThirtyDegreesRay on 2017/10/30 11:18:57
 */

public class AppOpener {

    public static void openInBrowser(@NonNull Context context, @NonNull String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent = createActivityChooserIntent(context, intent, uri);
        try{
            context.startActivity(intent);
        }catch (ActivityNotFoundException ae){
            Toasty.warning(context, context.getString(R.string.no_share_clients)).show();
        }catch (Exception e){
            Toasty.warning(context, context.getString(R.string.failed_to_open_url), Toast.LENGTH_LONG).show();
        }
    }

    public static void shareText(@NonNull Context context, @NonNull String text) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)));
        }catch (ActivityNotFoundException e){
            Toasty.warning(context, context.getString(R.string.no_share_clients)).show();
        }
    }

    public static void launchEmail(@NonNull Context context, @NonNull String email){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
        }catch (ActivityNotFoundException e){
            Toasty.warning(context, context.getString(R.string.no_email_clients)).show();
        }
    }

    public static void openInMarket(@NonNull Context context){
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_in_market)));
        }catch (ActivityNotFoundException e){
            Toasty.warning(context, context.getString(R.string.no_market_clients)).show();
        }
    }

    public static void launchUrl(@NonNull Context context, @NonNull Uri uri){
        if(StringUtils.isBlank(uri.toString())) return;
        String loginId;
        if(GitHubHelper.isImage(uri.toString())){
            ViewerActivity.show(context, uri.toString());
        } else if((loginId = GitHubHelper.getUserFromUrl(uri.toString())) != null){
            ProfileActivity.show((Activity) context, loginId);
        } else if(GitHubHelper.isRepoUrl(uri.toString())){
            String fullName = GitHubHelper.getRepoFullNameFromUrl(uri.toString());
            if(StringUtils.isBlank(fullName)){
                openInBrowser(context, uri.toString());
            } else {
                RepositoryActivity.show(context, fullName.split("/")[0], fullName.split("/")[1]);
            }
        } else if (GitHubHelper.isIssueUrl(uri.toString())) {
            IssueDetailActivity.show((Activity) context, uri.toString());
        } else {
            String url = uri.toString();
            openInBrowser(context, url);
        }
    }

    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){
                return true;
            }
        }
        return false;
    }

    private static Intent createActivityChooserIntent(Context context, Intent intent, Uri uri) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        final ArrayList<Intent> chooserIntents = new ArrayList<>();
        final String ourPackageName = context.getPackageName();

        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo resInfo : activities) {
            ActivityInfo info = resInfo.activityInfo;
            if (!info.enabled || !info.exported) {
                continue;
            }
            if (info.packageName.equals(ourPackageName)) {
                continue;
            }

            Intent targetIntent = new Intent(intent);
            targetIntent.setPackage(info.packageName);
            targetIntent.setDataAndType(uri, intent.getType());
            chooserIntents.add(targetIntent);
        }

        if (chooserIntents.isEmpty()) {
            return null;
        }

        final Intent lastIntent = chooserIntents.remove(chooserIntents.size() - 1);
        if (chooserIntents.isEmpty()) {
            // there was only one, no need to show the chooser
            return lastIntent;
        }

        Intent chooserIntent = Intent.createChooser(lastIntent, null);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                chooserIntents.toArray(new Intent[chooserIntents.size()]));
        return chooserIntent;
    }

}
