package com.yooiistudios.newsflow.ui;

import android.content.Context;
import android.content.res.Resources;

import com.yooiistudios.newsflow.R;

import java.util.Random;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 4. 10.
 * BottomRandomPalette
 *
 * 10개의 머티리얼 컬러를 우성이 큐레이팅해서 그 중 하나를 랜덤으로 뽑아줌
 *
 * material_pink_900
 * material_purple_900
 * material_cyan_900
 * material_yellow_900
 * material_amber_900
 * material_lime_900
 * material_green_900
 * material_teal_900
 * material_blue_900
 * material_red_900
 */
public class RandomMaterialColors {
    private RandomMaterialColors() { throw new AssertionError("You can't create this class!"); }

    public static int get(Context context) {
        Random r = new Random();
        int index = r.nextInt(10);

        Resources resources = context.getResources();
        switch (index) {
            case 0:
                return resources.getColor(R.color.material_pink_900);
            case 1:
                return resources.getColor(R.color.material_purple_900);
            case 2:
                return resources.getColor(R.color.material_cyan_900);
            case 3:
                return resources.getColor(R.color.material_yellow_900);
            case 4:
                return resources.getColor(R.color.material_amber_900);
            case 5:
                return resources.getColor(R.color.material_lime_900);
            case 6:
                return resources.getColor(R.color.material_green_900);
            case 7:
                return resources.getColor(R.color.material_teal_900);
            case 8:
                return resources.getColor(R.color.material_blue_900);
            case 9:
                return resources.getColor(R.color.material_red_900);
            default:
                return resources.getColor(R.color.material_blue_900);
        }
    }
}
