package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.MiniCheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import com.watabou.noosa.Camera;

import java.util.ArrayList;

public class WndChallenges extends Window {

    private static final int WIDTH      = 120;
    private static final int TTL_HEIGHT = 16;
    private static final int BTN_HEIGHT = 11;
    private static final int GAP        = 1;

    // 컬럼 사이 간격
    private static final int COL_GAP    = 4;

    // 화면 가장자리 여유 (윈도우가 화면 밖으로 나가지 않게)
    private static final int OUTER_MARGIN = 6;

    private boolean editable;
    private ArrayList<MiniCheckBox> boxes;

    public WndChallenges(int checked, boolean editable) {

        super();

        this.editable = editable;
        boxes = new ArrayList<>();

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12);
        title.hardlight(TITLE_COLOR);

        // ---- 화면 기준 최대 높이 계산 ----
        float maxH = Camera.main.height - OUTER_MARGIN * 2f;

        int n = Challenges.NAME_IDS.length;

        // 1열일 때 필요한 높이
        float singleH = TTL_HEIGHT + n * BTN_HEIGHT + Math.max(0, n - 1) * GAP;

        // 몇 열이 필요할지(최대 3열)
        int cols = 1;
        if (singleH > maxH && PixelScene.landscape()) {
            cols = (int)Math.ceil(singleH / maxH);
            if (cols < 2) cols = 2;
            if (cols > 3) cols = 3;
        }

        // 열 개수에 따라 한 열에 들어갈 row 수
        int rows = (int)Math.ceil(n / (float)cols);

        // 실제 윈도우 폭/열 폭 계산 (기본 WIDTH 기반)
        int winW = WIDTH * cols + COL_GAP * (cols - 1);
        int colW = WIDTH;

        // 혹시 화면 폭이 너무 좁으면 열 수를 줄임(안전장치)
        float maxW = Camera.main.width - OUTER_MARGIN * 2f;
        while (winW > maxW && cols > 1) {
            cols--;
            rows = (int)Math.ceil(n / (float)cols);
            winW = WIDTH * cols + COL_GAP * (cols - 1);
        }

        // 타이틀 위치는 최종 winW 기준으로
        title.setPos(
                (winW - title.width()) / 2f,
                (TTL_HEIGHT - title.height()) / 2f
        );
        PixelScene.align(title);
        add(title);

        // ---- 체크박스 배치 ----
        // row 기준 높이
        float contentH = TTL_HEIGHT + rows * BTN_HEIGHT + Math.max(0, rows - 1) * GAP;
        float winH = Math.min(contentH, maxH);

        float startY = TTL_HEIGHT;

        for (int i = 0; i < n; i++) {

            final String challenge = Challenges.NAME_IDS[i];

            int col = i / rows;
            int row = i % rows;

            float x = col * (colW + COL_GAP);
            float y = startY + row * (BTN_HEIGHT + GAP);

            MiniCheckBox cb = new MiniCheckBox(Messages.titleCase(Messages.get(Challenges.class, challenge)));
            cb.checked((checked & Challenges.MASKS[i]) != 0);
            cb.active = editable;

            cb.setRect(x, y, colW - 11, BTN_HEIGHT);
            add(cb);
            boxes.add(cb);

            IconButton info = new IconButton(Icons.get(Icons.MINI_INFO)) {
                @Override
                protected void onClick() {
                    super.onClick();
                    ShatteredPixelDungeon.scene().add(
                            new WndMessage(Messages.get(Challenges.class, challenge + "_desc"))
                    );
                }
            };
            info.setRect(cb.right(), y, 11, BTN_HEIGHT);
            add(info);
        }

        resize(winW, (int)winH);
    }

    @Override
    public void onBackPressed() {

        if (editable) {
            int value = 0;
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).checked()) {
                    value |= Challenges.MASKS[i];
                }
            }
            SPDSettings.challenges(value);
        }

        super.onBackPressed();
    }
}