package com.yooiistudios.newsflow.model;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.core.connector.ConnectorException;
import com.yooiistudios.newsflow.core.connector.ConnectorResult;
import com.yooiistudios.newsflow.core.connector.DownloadRequest;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 12.
 *
 * PairingTask
 *  토큰을 가지고 커넥터 서버에 "데이터가 들어와 있나 Active 하게 체크" 하는 서비스
 */
public class PairingTask extends AsyncTask<Void, Void, ConnectorResult> {
    // 2 min
    private static final long REQUEST_THRESHOLD_MILLI = 2 * 60 * 1000;
    // 2 sec
//    private static final long REQUEST_THRESHOLD_MILLI = 2 * 1000;

    private DownloadRequest mRequest;
    private long mRequestStartTimeInMilli;

    public PairingTask(DownloadRequest request) {
        mRequest = request;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mRequestStartTimeInMilli = System.currentTimeMillis();
    }

    @Override
    protected ConnectorResult doInBackground(Void... params) {
        ConnectorResult result = null;
        while (shouldTryMore(result)) {
            try {
                Thread.sleep(1000);
                result = mRequest.execute();
            } catch (ConnectorException e) {
                break;
            } catch (InterruptedException ignored) {
            }
        }
        if (result == null) {
            result = ConnectorResult.getErrorObject();
        }
        return result;
    }

    private boolean shouldTryMore(ConnectorResult result) {
        return !isSuccess(result) && !isExpired();
    }

    private boolean isSuccess(ConnectorResult result) {
        return result != null && result.resultCode == ConnectorResult.RC_SUCCESS;
    }

    private boolean isExpired() {
        long timePast = System.currentTimeMillis() - mRequestStartTimeInMilli;
        return timePast > REQUEST_THRESHOLD_MILLI || isCancelled();
    }

    @Override
    protected void onPostExecute(ConnectorResult result) {
        super.onPostExecute(result);

        if (!isCancelled()) {
            mRequest.handleResult(result);
        }
    }
}
