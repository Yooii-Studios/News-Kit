package com.yooiistudios.newskit.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yooiistudios.newskit.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;

/**
 * Created by StevenKim in MNSettingActivityProject from Yooii Studios Co., LTD. on 2014. 1. 8.
 *
 * MNLicenseListViewAdapter
 */
public class LicenseListAdapter extends BaseAdapter {
    private Context context;

    @SuppressWarnings("UnusedDeclaration")
    private LicenseListAdapter() {}
    public LicenseListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        // news kit
        // butterknife
        // lombok
        // jsoup
        // gson
        // joda-time
        // Material Dialogs
        // Floating Action Button
        // Circular Reveal
        // nineoldandroids
        // guava
        return 11;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.more_info_license_item, parent, false);

        LicenseItemViewHolder viewHolder = new LicenseItemViewHolder(convertView);

        switch (position) {
            case 0:
                viewHolder.getTitleTextView().setText("News Kit");
                viewHolder.getLinkTextView().setText("http://yooiistudios.com/termsofservice.html");
                viewHolder.getDetailTextView().setText("Yooii Studios Co., LTD.");
                break;
            case 1:
                viewHolder.getTitleTextView().setText("Butter Knife");
                viewHolder.getLinkTextView().setText("http://jakewharton.github.io/butterknife/");
                viewHolder.getDetailTextView().setText("Copyright 2013 Jake Wharton\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.");
                break;
            case 2:
                viewHolder.getTitleTextView().setText("Lombok");
                viewHolder.getLinkTextView().setText("http://projectlombok.org/");
                viewHolder.getDetailTextView().setText("Copyright © 2009-2013 The Project Lombok Authors, licensed under the MIT license.");
                break;
            case 3:
                viewHolder.getTitleTextView().setText("jsoup");
                viewHolder.getLinkTextView().setText("http://jsoup.org");
                viewHolder.getDetailTextView().setText("The MIT License\n" +
                        "Copyright © 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
                break;
            case 4:
                viewHolder.getTitleTextView().setText("Gson");
                viewHolder.getLinkTextView().setText("http://code.google.com/p/google-gson/");
                viewHolder.getDetailTextView().setText("Apache License 2.0");
                break;
            case 5:
                viewHolder.getTitleTextView().setText("Joda-Time - Java date and time API");
                viewHolder.getLinkTextView().setText("http://www.joda.org/joda-time/");
                viewHolder.getDetailTextView().setText("Apache License\n" +
                        "                           Version 2.0, January 2004\n" +
                        "                        http://www.apache.org/licenses/\n" +
                        "\n" +
                        "   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION");
                break;
            case 6:
                viewHolder.getTitleTextView().setText("Material Dialogs");
                viewHolder.getLinkTextView().setText("https://github.com/afollestad/material-dialogs");
                viewHolder.getDetailTextView().setText("The MIT License (MIT)\n" +
                        "\n" +
                        "Copyright (c) 2015 Aidan Michael Follestad\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                        "of this software and associated documentation files (the \"Software\"), to deal\n" +
                        "in the Software without restriction, including without limitation the rights\n" +
                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                        "copies of the Software, and to permit persons to whom the Software is\n" +
                        "furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all\n" +
                        "copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                        "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                        "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                        "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                        "SOFTWARE.");
                break;
            case 7:
                viewHolder.getTitleTextView().setText("FloatingActionButton");
                viewHolder.getLinkTextView().setText("https://github.com/makovkastar/FloatingActionButton");
                viewHolder.getDetailTextView().setText("The MIT License (MIT)\n" +
                        "\n" +
                        "Copyright (c) 2014 Oleksandr Melnykov\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                        "of this software and associated documentation files (the \"Software\"), to deal\n" +
                        "in the Software without restriction, including without limitation the rights\n" +
                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                        "copies of the Software, and to permit persons to whom the Software is\n" +
                        "furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all\n" +
                        "copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                        "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                        "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                        "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                        "SOFTWARE.");
                break;
            case 8:
                viewHolder.getTitleTextView().setText("CircularReveal");
                viewHolder.getLinkTextView().setText("https://github.com/ozodrukh/CircularReveal");
                viewHolder.getDetailTextView().setText("The MIT License (MIT)\n" +
                        "\n" +
                        "Copyright (c) 2014 Abdullaev Ozodrukh\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                        "of this software and associated documentation files (the \"Software\"), to deal\n" +
                        "in the Software without restriction, including without limitation the rights\n" +
                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                        "copies of the Software, and to permit persons to whom the Software is\n" +
                        "furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in\n" +
                        "all copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                        "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                        "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                        "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
                        "THE SOFTWARE.");
                break;
            case 9:
                viewHolder.getTitleTextView().setText("NineOldAndroids");
                viewHolder.getLinkTextView().setText("http://nineoldandroids.com");
                viewHolder.getDetailTextView().setText("© 2012 Jake Wharton — @JakeWharton · +JakeWharton\n" +
                        "Developed and distributed under the Apache License, Version 2.0.");
                break;
            case 10:
                viewHolder.getTitleTextView().setText("Guava");
                viewHolder.getLinkTextView().setText("https://code.google.com/p/guava-libraries/");
                viewHolder.getDetailTextView().setText("Apache License 2.0");
                break;
        }
        return convertView;
    }

    /**
     * ViewHolder
     */
    static class LicenseItemViewHolder {
        @Getter @InjectView(R.id.more_info_license_item_inner_layout) RelativeLayout innerLayout;
        @Getter @InjectView(R.id.more_info_license_item_title_textview) TextView titleTextView;
        @Getter @InjectView(R.id.more_info_license_item_link_textview) TextView linkTextView;
        @Getter @InjectView(R.id.more_info_license_item_detail_textview) TextView detailTextView;

        public LicenseItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
