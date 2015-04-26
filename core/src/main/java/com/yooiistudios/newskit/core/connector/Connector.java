package com.yooiistudios.newskit.core.connector;

import android.os.AsyncTask;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * Connector
 *  유이 커넥터 통신
 */
public class Connector {
    public static void execute(ConnectorRequest request) {
        new ConnectorTask(request).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                result = ConnectorResult.createErrorObject();
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