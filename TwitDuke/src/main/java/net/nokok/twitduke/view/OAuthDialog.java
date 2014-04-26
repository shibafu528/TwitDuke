/*
 * The MIT License
 *
 * Copyright 2014 noko <nokok.kz at gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.nokok.twitduke.view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.nokok.twitduke.component.OKCancelButtonPanel;
import net.nokok.twitduke.component.basic.TWFrame;
import net.nokok.twitduke.component.basic.TWLabel;
import net.nokok.twitduke.component.basic.TWPanel;
import net.nokok.twitduke.component.basic.TWTextField;
import net.nokok.twitduke.core.api.view.Dialog;
import net.nokok.twitduke.core.api.view.DialogResultListener;

/**
 * OAuthダイアログはOAuth認証時に表示されるPINの入力画面を担当するクラスです。
 * このクラスはあくまでViewのみで実際の処理はDialogResultListener<Integer>実装クラスで
 * 行われます。
 * <p>
 * @author noko <nokok.kz at gmail.com>
 */
public class OAuthDialog implements Dialog<Integer> {

    private final JFrame frame;
    private final OKCancelButtonPanel okCancelPanel = new OKCancelButtonPanel();
    private DialogResultListener<Integer> dialogResultListener;

    public OAuthDialog() {
        frame = new TWFrame("認証して下さい");
        JTextField textField = new TWTextField();
        frame.setLayout(new BorderLayout());
        frame.add(createNorthPanel(), BorderLayout.NORTH);
        frame.add(textField, BorderLayout.CENTER);
        frame.add(okCancelPanel, BorderLayout.SOUTH);
        frame.pack();

        okCancelPanel.addOKButtonAction(e -> {
            int pin = Integer.parseInt(textField.getText());
            dialogResultListener.okButtonPushed(pin);
        });

        okCancelPanel.addCancelButtonAction(e -> dialogResultListener.cancelButtonPushed());
    }

    private JPanel createNorthPanel() {
        JPanel panel = new TWPanel();
        panel.add(new TWLabel("表示された数字を入力して下さい"));
        return panel;
    }

    @Override
    public void dispose() {
        frame.dispose();
    }

    @Override
    public void setDialogResultListener(DialogResultListener<Integer> dialogResultListener) {
        this.dialogResultListener = dialogResultListener;
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }
}