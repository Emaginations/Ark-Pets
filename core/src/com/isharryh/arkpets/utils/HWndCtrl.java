/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import java.util.ArrayList;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;


public class HWndCtrl {
    static ArrayList<HWndCtrl> windowList;
    final public HWND hWnd;
    final public String windowText;
    final public Pointer windowPointer;
    final public int posTop;
    final public int posBottom;
    final public int posLeft;
    final public int posRight;
    final public int windowWidth;
    final public int windowHeight;

    /** HWnd Controller instance.
     * @param $hWnd The handle of the window.
     */
    public HWndCtrl(HWND $hWnd) {
        hWnd = $hWnd;
        windowText = getWindowText(hWnd);
        windowPointer = getWindowIdx($hWnd);
        RECT rect = getWindowRect(hWnd);
        posTop = rect.top;
        posBottom = rect.bottom;
        posLeft = rect.left;
        posRight = rect.right;
        windowWidth = posRight-posLeft;
        windowHeight = posBottom-posTop;
    }

    /** Empty HWnd Controller instance.
     */
    public HWndCtrl() {
        hWnd = null;
        windowText = null;
        windowPointer = null;
        posTop = 0;
        posBottom = 0;
        posLeft = 0;
        posRight = 0;
        windowWidth = posRight-posLeft;
        windowHeight = posBottom-posTop;
    }

    /** Judge whether the window is visible.
     * @return true=visible, false=invisible.
     */
    public boolean isVisible() {
        if (!User32.INSTANCE.IsWindowVisible(hWnd) || !User32.INSTANCE.IsWindowEnabled(hWnd))
            return false;
        if (windowWidth <= 0 || windowHeight <= 0|| posBottom < 0 || posRight < 0)
            return false;
        return true;
    }

    /** Get the current list of windows.
     * @param $only_visible Only the visible window allowed.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<HWndCtrl> getWindowList(boolean $only_visible) {
        windowList = new ArrayList<HWndCtrl>();
        if ($only_visible) {
            User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
                @Override
                public boolean callback(HWND hWnd, Pointer arg1) {
                    if (User32.INSTANCE.IsWindow(hWnd) && isVisible(hWnd))
                        windowList.add(new HWndCtrl(hWnd));
                    return true;
                }
            }, null);
        } else {
            User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
                @Override
                public boolean callback(HWND hWnd, Pointer arg1) {
                    if (User32.INSTANCE.IsWindow(hWnd))
                        windowList.add(new HWndCtrl(hWnd));
                    return true;
                }
            }, null);
        }
		return windowList;
    }

    static private boolean isVisible(HWND $hWnd) {
        if (!User32.INSTANCE.IsWindowVisible($hWnd) || !User32.INSTANCE.IsWindowEnabled($hWnd))
            return false;
        RECT rect = getWindowRect($hWnd);
        int posTop = rect.top;
        int posBottom = rect.bottom;
        int posLeft = rect.left;
        int posRight = rect.right;
        if (posRight <= posLeft || posBottom <= posTop || posBottom < 0 || posRight < 0)
            return false;
        return true;
    }

    static private Pointer getWindowIdx(HWND $hWnd) {
        return $hWnd.getPointer();
    }

    static private String getWindowText(HWND $hWnd) {
        char[] text = new char[512];
		User32.INSTANCE.GetWindowText($hWnd, text, 512);
		return Native.toString(text);
    }

    static private RECT getWindowRect(HWND $hWnd) {
        RECT rect = new RECT();
		User32.INSTANCE.GetWindowRect($hWnd, rect);
        return rect;
    }
}
