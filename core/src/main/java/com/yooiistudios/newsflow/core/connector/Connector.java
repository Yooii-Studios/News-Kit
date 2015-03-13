package com.yooiistudios.newsflow.core.connector;

import android.os.AsyncTask;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * Connector
 *  유이 커넥터 통신
 */
public class Connector {
    public static void upload(UploadRequest request) {
        new ConnectorTask(request).execute();
    }

//    public static void download(DownloadRequest request) {
//        new ConnectorTask(request).execute();
//    }

    public static void getUniqueToken(GetUniqueTokenRequest request) {
        new ConnectorTask(request).execute();
    }

    private static class ConnectorTask extends AsyncTask<Void, Void, ConnectorResult> {
        private ConnectorRequest mRequest;

        public ConnectorTask(ConnectorRequest request) {
            mRequest = request;
        }

        @Override
        protected ConnectorResult doInBackground(Void... params) {
            ConnectorResult result;
            try {
                result = mRequest.execute();
            } catch (ConnectorException e) {
                e.printStackTrace();
                result = ConnectorResult.getErrorObject();
            }
            return result;
        }

        @Override
        protected void onPostExecute(ConnectorResult result) {
            super.onPostExecute(result);

            mRequest.handleResult(result);
        }
    }
}