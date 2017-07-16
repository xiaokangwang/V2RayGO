package org.kkdev.v2raygo;

import android.content.Intent;
import android.os.Bundle;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by shelikhoo on 2/23/17.
 */

public class NavDrawer extends MaterialNavigationDrawer {
    @Override
    public void init(Bundle savedInstanceState) {
        this.addSection(newSection("Brief", new FragmentStatusBeief()));
        this.addSection(newSection("Configure", new ProxyConfigureFileFragment()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
