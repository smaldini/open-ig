/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.core.InfoBarRegions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class StarmapRenderer extends JComponent implements MouseMotionListener, MouseListener, MouseWheelListener {
	/** The serial version UID. */
	private static final long serialVersionUID = -4832071241159647010L;
	/** The graphics objects. */
	private final StarmapGFX gfx;
	/** The area of the horizontal scrollbar. */
	private Rectangle hscrollRect = new Rectangle();
	/** The horizontal scrollbar knobs location. */
	private Rectangle hknobRect = new Rectangle();
	/** The area of the vertical scrollbar. */
	private Rectangle vscrollRect = new Rectangle();
	/** The vertical scrollbar knob location. */
	private Rectangle vknobRect = new Rectangle();
	/** The position of the ship control rectangle. */
	private Rectangle shipControlRect = new Rectangle();
	/** The position of the minimap rectangle. */
	private Rectangle minimapRect = new Rectangle();
	/** The position of the main map rectangle. */
	private Rectangle mapRect = new Rectangle();
	/** The width when the last time the component was rendered. */
	private int lastWidth = -1;
	/** The height when the last time the component was rendered. */
	private int lastHeight = -1;
	/** The maximum value of the vertical scroller. Minimum is always 0. */
	private int vscrollMax = 0;
	/** Current pixel value of the vertical scrollbar. */
	private int vscrollValue = 0;
	/** The maximum value of the horizontal scroller. Minimum is always 0. */
	private int hscrollMax = 0;
	/** Current pixel value of the horizontal scrollbar. */
	private int hscrollValue = 0;
	/** Determines zoom factor where 1.0 means the original size, 0.5 the half size. */
	private float zoomFactor = 1f;
	/** Set to true if the user performs the map movement via the mouse. */
	private boolean mapDragMode;
	/** The last mouse coordinate. */
	private int lastMouseX;
	/** The last mouse coordinate. */
	private int lastMouseY;
	/** The actual pixel offset for the background map. */
//		private int mapX;
	/** The actual pixel offset for the background map. */
//		private int mapY;
	/** The vertical drag mode is on. */
	private boolean verticalDragMode;
	/** The horizontal drag mode is on. */
	private boolean horizontalDragMode;
	/** The horizontal scroll factor. */
	private float hscrollFactor;
	/** The vertical scroll factor. */
	private float vscrollFactor;
	/** The common graphics objects. */
	private CommonGFX cgfx;
	private Rectangle bottomLeftRect = new Rectangle();
	private Rectangle bottomFillerRect = new Rectangle();
	private Rectangle bottomRightRect = new Rectangle();
	private Rectangle rightTopRect = new Rectangle();
	private Rectangle rightFillerRect = new Rectangle();
	private Rectangle rightBottomRect = new Rectangle();
	private InfoBarRegions infoBarRect = new InfoBarRegions();
	private Btn btnColonyPrev;
	private Btn btnColonyNext;
	/** The colonies text area. */
	private Rectangle colonies = new Rectangle();
	private Btn btnColony;
	private Btn btnEquipment;
	private Btn btnEquipmentPrev;
	private Btn btnEquipmentNext;
	/** The equipment text area. */
	private Rectangle equipments = new Rectangle();
	private Btn btnInfo;
	private Btn btnBridge;
	private Btn btnMagnify;
	/** The various buttons. */
	private final List<Btn> buttons = new ArrayList<Btn>();
	/** The magnification direction: true-in, false-out. */
	private boolean magnifyDirection;
	/** The magnification factors. */
	private int[] magnifyFactors = { /*5, 6, 7, 8, 9, 10, 12, 13,*/ 15, 17, 20, 24, 30 };
	/** The current magnification index. */
	private int magnifyIndex = magnifyFactors.length - 1;
	/** Show ship controls. */
	private boolean showShipControls;
	private Btn btnColonize;
	private Btn btnRadars;
	private Btn btnFleets;
	private Btn btnGrids;
	private Btn btnStars;
	private Btn btnName;
	/** Ship move button. */
	private Btn btnMove;
	/** Ship attack button. */
	private Btn btnAttack;
	/** Ship stop buttom. */
	private Btn btnStop;
	/** The display name modes: None, Colony, Fleets, Both. */
	private int nameMode;
	/** Buttons which change state on click.*/
	private final List<Btn> toggleButtons = new ArrayList<Btn>();
	/** The mouse wheel scrolling amount. */
	private static final int SCROLL_AMOUNT = 20;
	private Btn btnSatellite;
	private Btn btnSpySat1;
	private Btn btnSpySat2;
	private Btn btnHubble2;
	public boolean showSatellites = true;
	/** Constructor. */
	public StarmapRenderer(StarmapGFX gfx, CommonGFX cgfx) {
		super();
		this.gfx = gfx;
		this.cgfx = cgfx;
		setDoubleBuffered(true);
		setOpaque(true);
		//setCursor(gfx.cursors.target);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		initActions();
	}
	@Override
	public void paint(Graphics g) {
		long t = System.nanoTime();
		Graphics2D g2 = (Graphics2D)g;
		// draw top and bottom bar four corners
		int w = getWidth();
		int h = getHeight();
		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
			zoom(zoomFactor);
			updateScrollKnobs();
		}
		
		cgfx.renderInfoBars(this, g2);
		
		// draw inner area four corners
		g2.drawImage(gfx.contents.bottomLeft, bottomLeftRect.x, bottomLeftRect.y, null);
		g2.drawImage(gfx.contents.rightTop, rightTopRect.x, rightTopRect.y, null);

		g2.drawImage(gfx.contents.bottomRight, bottomRightRect.x, bottomRightRect.y, null);
		g2.drawImage(gfx.contents.rightBottom, rightBottomRect.x, rightBottomRect.y, null);
		// check if the rendering width is greater than the default 640
		// if so, draw the link lines
		if (bottomFillerRect.width > 0) {
			// inner content filler
			Paint p = g2.getPaint();
			g2.setPaint(new TexturePaint(gfx.contents.bottomFiller, new Rectangle(bottomFillerRect.x, bottomFillerRect.y, 2, bottomFillerRect.height)));
			g2.fill(bottomFillerRect);
			g2.setPaint(p);
			/*
			for (int i = gfx.contents.bottomLeft.getWidth(); i < w - gfx.contents.bottomRight.getWidth(); i += 2) {
				g2.drawImage(gfx.contents.bottomFiller, i, h - bh, null);
			}
			*/
		}
		if (h > 480) {
			Paint p = g2.getPaint();
			g2.setPaint(new TexturePaint(gfx.contents.rightFiller, new Rectangle(rightFillerRect.x, rightFillerRect.y, rightFillerRect.width, 2)));
			g2.fill(rightFillerRect);
			g2.setPaint(p);
//				for (int i = cgfx.top.right.getHeight() + gfx.contents.rightTop.getHeight(); i < h - bh - gfx.contents.rightBottom.getHeight(); i += 2) {
//					g2.drawImage(gfx.contents.rightFiller, w - gfx.contents.rightFiller.getWidth(), i, null);
//				}
		}
		// draw inner controls

		g2.setColor(Color.BLACK);
		g2.fillRect(hscrollRect.x, hscrollRect.y, hscrollRect.width, hscrollRect.height);
		g2.fillRect(vscrollRect.x, vscrollRect.y, vscrollRect.width, vscrollRect.height);
		
		// paint horizontal scrollbar
		g2.drawImage(gfx.contents.hscrollLeft, hknobRect.x, hknobRect.y, null);
		g2.drawImage(gfx.contents.hscrollRight, hknobRect.x + hknobRect.width - gfx.contents.hscrollRight.getWidth(), hscrollRect.y, null);
		g2.drawImage(gfx.contents.vscrollTop, vknobRect.x, vknobRect.y, null);
		g2.drawImage(gfx.contents.vscrollBottom, vknobRect.x, vknobRect.y + vknobRect.height - gfx.contents.vscrollBottom.getHeight(), null);
		
		Paint p = g2.getPaint();
		// horizontal scrollbar middle
		g2.setPaint(new TexturePaint(gfx.contents.hscrollFiller, new Rectangle(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, gfx.contents.hscrollFiller.getWidth(), gfx.contents.hscrollFiller.getHeight())));
		g2.fillRect(hknobRect.x + gfx.contents.hscrollLeft.getWidth(), hscrollRect.y, hknobRect.width - gfx.contents.hscrollLeft.getWidth() - gfx.contents.hscrollRight.getWidth(), gfx.contents.hscrollFiller.getHeight());
		g2.setPaint(new TexturePaint(gfx.contents.vscrollFiller, new Rectangle(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), gfx.contents.vscrollFiller.getHeight())));
		g2.fillRect(vknobRect.x, vknobRect.y + gfx.contents.vscrollTop.getHeight(), gfx.contents.vscrollFiller.getWidth(), vknobRect.height - gfx.contents.vscrollTop.getHeight() - gfx.contents.vscrollBottom.getHeight());
		
		g2.setPaint(p);
		
		// draw minimap
		g2.drawImage(gfx.contents.minimap, minimapRect.x, minimapRect.y, null);
		
		// draw the entire map in a clipping rect
		g2.setColor(gfx.contents.mapBackground);
		//g2.setColor(Color.YELLOW);
		g2.fillRect(mapRect.x, mapRect.y, mapRect.width, mapRect.height);
		Shape sp = g2.getClip();
		g2.setClip(mapRect);
		AffineTransform af = g2.getTransform();
		int mx = -(int)(hscrollValue * hscrollFactor / zoomFactor);
		int my = -(int)(vscrollValue * vscrollFactor / zoomFactor); 
		// if the viewport is much bigger than the actual image, lets center it
		if (mapRect.width > gfx.contents.fullMap.getWidth() * zoomFactor) {
			mx = (int)((mapRect.width / zoomFactor - gfx.contents.fullMap.getWidth()) / 2);
		}
		if (mapRect.height > gfx.contents.fullMap.getHeight() * zoomFactor) {
			my = (int)((mapRect.height / zoomFactor - gfx.contents.fullMap.getHeight()) / 2);
		}
		g2.translate(mapRect.x, mapRect.y);
		g2.scale(zoomFactor, zoomFactor);
		g2.drawImage(gfx.contents.fullMap, mx, my, null);
		g2.setTransform(af);
		
		g2.setClip(sp);
		
		// ----------------------------------------------------------------
		// RENDER BUTTONS
		// ----------------------------------------------------------------
		
		g2.setColor(Color.BLACK);
		if (btnColonyPrev.disabled) {
			g2.drawImage(gfx.btnPrevDisabled, btnColonyPrev.rect.x, btnColonyPrev.rect.y, null);
		} else
		if (btnColonyPrev.down) {
			Rectangle r = btnColonyPrev.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnPrevDown, r.x, r.y + 1, null);
		}
		if (btnColonyNext.disabled) {
			g2.drawImage(gfx.btnNextDisabled, btnColonyNext.rect.x, btnColonyNext.rect.y + 1, null);
		} else
		if (btnColonyNext.down) {
			Rectangle r = btnColonyNext.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnNextDown, r.x, r.y + 1, null);
		}
		if (btnColony.down) {
			g2.drawImage(gfx.btnColonyDown, btnColony.rect.x, btnColony.rect.y, null);
		}
		
		if (btnEquipmentPrev.disabled) {
			g2.drawImage(gfx.btnPrevDisabled, btnEquipmentPrev.rect.x, btnEquipmentPrev.rect.y, null);
		} else
		if (btnEquipmentPrev.down) {
			Rectangle r = btnEquipmentPrev.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnPrevDown, r.x, r.y + 1, null);
		}
		if (btnEquipmentNext.disabled) {
			g2.drawImage(gfx.btnNextDisabled, btnEquipmentNext.rect.x, btnEquipmentNext.rect.y, null);
		} else
		if (btnEquipmentNext.down) {
			Rectangle r = btnEquipmentNext.rect;
			g2.drawLine(r.x, r.y, r.x + r.width, r.y);
			g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width, r.y + r.height - 1);
			g2.drawImage(gfx.btnNextDown, r.x, r.y + 1, null);
		}
		if (btnEquipment.disabled) {
			g2.drawImage(gfx.btnEquipmentDisabled, btnEquipment.rect.x, btnEquipment.rect.y, null);
		} else
		if (btnEquipment.down) {
			g2.drawImage(gfx.btnEquipmentDown, btnEquipment.rect.x, btnEquipment.rect.y, null);
		}
		
		if (btnInfo.down) {
			g2.drawImage(gfx.btnInfoDown, btnInfo.rect.x, btnInfo.rect.y, null);
		}
		if (btnBridge.down) {
			g2.drawImage(gfx.btnBridgeDown, btnBridge.rect.x, btnBridge.rect.y, null);
		}
		if (btnMagnify.disabled) {
			g2.drawImage(gfx.btnMagnifyDisabled, btnMagnify.rect.x, btnMagnify.rect.y, null);
		} else
		if (btnMagnify.down) {
			g2.drawImage(gfx.btnMagnifyLight, btnMagnify.rect.x, btnMagnify.rect.y, null);
		}

		if (showShipControls) {
			if (btnMove.down) {
				g2.drawImage(gfx.btnMoveLight, btnMove.rect.x, btnMove.rect.y, null);
			}
			if (btnAttack.down) {
				g2.drawImage(gfx.btnAttackLight, btnAttack.rect.x, btnAttack.rect.y, null);
			}
			if (btnStop.down) {
				g2.drawImage(gfx.btnStopLight, btnStop.rect.x, btnStop.rect.y, null);
			}
		} else 
		if (showSatellites) {
			g2.fillRect(shipControlRect.x, shipControlRect.y, shipControlRect.width, shipControlRect.height);
			if (btnSatellite.visible) {
				g2.drawImage(gfx.btnAddSat, btnSatellite.rect.x, btnSatellite.rect.y, null);
			}
			if (btnSpySat1.visible) {
				g2.drawImage(gfx.btnAddSpySat1, btnSpySat1.rect.x, btnSpySat1.rect.y, null);
			}
			if (btnSpySat2.visible) {
				g2.drawImage(gfx.btnAddSpySat2, btnSpySat2.rect.x, btnSpySat2.rect.y, null);
			}
			if (btnHubble2.visible) {
				g2.drawImage(gfx.btnAddHubble2, btnHubble2.rect.x, btnHubble2.rect.y, null);
			}
		} else {
			g2.fillRect(shipControlRect.x, shipControlRect.y, shipControlRect.width, shipControlRect.height);
		}

		
		if (btnColonize.visible) {
			if (btnColonize.disabled) {
				g2.drawImage(gfx.btnColoniseDisabled, btnColonize.rect.x, btnColonize.rect.y, null);
			} else {
				g2.drawImage(gfx.btnColonise, btnColonize.rect.x, btnColonize.rect.y, null);
			}
		}
		
		if (btnRadars.down) {
			g2.drawImage(gfx.btnRadarsLight, btnRadars.rect.x, btnRadars.rect.y, null);
		} else {
			g2.drawImage(gfx.btnRadars, btnRadars.rect.x, btnRadars.rect.y, null);
		}
		if (btnFleets.down) {
			g2.drawImage(gfx.btnFleetsLight, btnFleets.rect.x, btnFleets.rect.y, null);
		} else {
			g2.drawImage(gfx.btnFleets, btnFleets.rect.x, btnFleets.rect.y, null);
		}
		if (btnStars.down) {
			g2.drawImage(gfx.btnStarsLight, btnStars.rect.x, btnStars.rect.y, null);
		} else {
			g2.drawImage(gfx.btnStars, btnStars.rect.x, btnStars.rect.y, null);
		}
		if (btnGrids.down) {
			g2.drawImage(gfx.btnGridsLight, btnGrids.rect.x, btnGrids.rect.y, null);
		} else {
			g2.drawImage(gfx.btnGrids, btnGrids.rect.x, btnGrids.rect.y, null);
		}
		
		BufferedImage img = null;
		switch (nameMode) {
		case 0:
			img = gfx.btnNameOff;
			break;
		case 1:
			img = gfx.btnNameColony;
			break;
		case 2:
			img = gfx.btnNameFleets;
			break;
		case 3:
			img = gfx.btnNameBoth;
			break;
		}
		g2.drawImage(img, btnName.rect.x, btnName.rect.y, null);
		t = System.nanoTime() - t;
		//System.out.printf("%.2f frame/s%n", 1E9 / t);
	}
	/** Recalculate the region coordinates. */
	private void updateRegions() {
		cgfx.updateRegions(this, infoBarRect);
		int w = getWidth();
		int h = getHeight();
		int bh = cgfx.bottom.left.getHeight() + gfx.contents.bottomLeft.getHeight();
		// fix scrollbar area colors
		hscrollRect.x = 3;
		hscrollRect.y = h - bh + 3;
		hscrollRect.width = w - 142;
		hscrollRect.height = 18;

		vscrollRect.x = w - gfx.contents.rightBottom.getWidth() + 3;
		vscrollRect.y = cgfx.top.right.getHeight() + 3;
		vscrollRect.width = 18;
		vscrollRect.height = h - bh - cgfx.top.right.getHeight() - 7;

		// fix ship control area if the ship area is not visible
		shipControlRect.x = w - 355;
		shipControlRect.y = h - bh + 28;
		shipControlRect.width = 106;
		shipControlRect.height = 83;
		
		// minimap rectangle
		minimapRect.x = w - 133;
		minimapRect.y = h - 109 - cgfx.bottom.right.getHeight();
		minimapRect.width = 131;
		minimapRect.height = 108;
		
		mapRect.x = 0;
		mapRect.y = cgfx.top.left.getHeight();
		mapRect.width = w - gfx.contents.rightTop.getWidth();
		mapRect.height = h - cgfx.top.left.getHeight() - cgfx.bottom.left.getHeight() - gfx.contents.bottomLeft.getHeight();
		
		bottomLeftRect.x = 0;
		bottomLeftRect.y = h - cgfx.bottom.left.getHeight() - gfx.contents.bottomLeft.getHeight();
		bottomLeftRect.width = gfx.contents.bottomLeft.getWidth();
		bottomLeftRect.height = gfx.contents.bottomLeft.getHeight();
		
		bottomRightRect.x = w - gfx.contents.bottomRight.getWidth();
		bottomRightRect.y = bottomLeftRect.y;
		bottomRightRect.width = gfx.contents.bottomRight.getWidth();
		bottomRightRect.height = gfx.contents.bottomRight.getHeight();
		
		bottomFillerRect.x = bottomLeftRect.x + bottomLeftRect.width;
		bottomFillerRect.y = bottomLeftRect.y;
		bottomFillerRect.width = bottomRightRect.x - bottomFillerRect.x;
		bottomFillerRect.height = gfx.contents.bottomFiller.getHeight();
		
		rightTopRect.x = w - gfx.contents.rightTop.getWidth();
		rightTopRect.y = cgfx.top.right.getHeight();
		rightTopRect.width = gfx.contents.rightTop.getWidth();
		rightTopRect.height = gfx.contents.rightTop.getHeight();
		
		rightBottomRect.x = rightTopRect.x;
		rightBottomRect.y = bottomRightRect.y - gfx.contents.rightBottom.getHeight();
		rightBottomRect.width = gfx.contents.rightBottom.getWidth();
		rightBottomRect.height = gfx.contents.rightBottom.getHeight();
		
		rightFillerRect.x = rightTopRect.x;
		rightFillerRect.y = rightTopRect.y + rightTopRect.height;
		rightFillerRect.width = gfx.contents.rightFiller.getWidth();
		rightFillerRect.height = rightBottomRect.y - rightFillerRect.y;
		
		// Update button locations
		
		btnColonyPrev.rect.x = w - 105;
		btnColonyPrev.rect.y = rightTopRect.y + 5;
		btnColonyPrev.rect.width = 50;
		btnColonyPrev.rect.height = 20;
		
		btnColonyNext.rect.x = btnColonyPrev.rect.x + 52;
		btnColonyNext.rect.y = btnColonyPrev.rect.y;
		btnColonyNext.rect.width = 50;
		btnColonyNext.rect.height = 20;
		
		colonies.x = btnColonyPrev.rect.x - 2;
		colonies.y = btnColonyPrev.rect.y + btnColonyPrev.rect.height;
		colonies.width = 105;
		colonies.height = rightBottomRect.y + 16 - colonies.y;
		
		btnColony.rect.x = colonies.x + 1;
		btnColony.rect.y = colonies.y + colonies.height;
		btnColony.rect.width = colonies.width - 2;
		btnColony.rect.height = 28;
		
		btnEquipmentPrev.rect.x = btnColonyPrev.rect.x;
		btnEquipmentPrev.rect.y = btnColony.rect.y + btnColony.rect.height + 7;
		btnEquipmentPrev.rect.width = 50;
		btnEquipmentPrev.rect.height = 20;
		
		btnEquipmentNext.rect.x = btnEquipmentPrev.rect.x + 52;
		btnEquipmentNext.rect.y = btnEquipmentPrev.rect.y;
		btnEquipmentNext.rect.width = 50;
		btnEquipmentNext.rect.height = 20;
		
		equipments.x = colonies.x;
		equipments.y = btnEquipmentPrev.rect.y + btnEquipmentPrev.rect.height;
		equipments.width = 105;
		equipments.height = 33;
		
		btnEquipment.rect.x = btnColony.rect.x;
		btnEquipment.rect.y = equipments.y + equipments.height;
		btnEquipment.rect.width = 103;
		btnEquipment.rect.height = 28;
		
		btnInfo.rect.x = btnColony.rect.x + 1;
		btnInfo.rect.y = btnEquipment.rect.y + 105;
		btnInfo.rect.width = 102;
		btnInfo.rect.height = 39;
		
		btnBridge.rect.x = btnInfo.rect.x;
		btnBridge.rect.y = btnInfo.rect.y + btnInfo.rect.height;
		btnBridge.rect.width = 102;
		btnBridge.rect.height = 39;
		
		btnMagnify.rect.x = 69 + btnEquipment.rect.x;
		btnMagnify.rect.y = 35 + btnEquipment.rect.y;
		btnMagnify.rect.width = 33;
		btnMagnify.rect.height = 64;
		
		btnColonize.rect.x = w - 245;
		btnColonize.rect.y = bottomLeftRect.y + 30;
		btnColonize.rect.width = 108;
		btnColonize.rect.height = 15;
		
		btnRadars.rect.x = btnColonize.rect.x;
		btnRadars.rect.y = btnColonize.rect.y + 21;
		btnRadars.rect.width = 53;
		btnRadars.rect.height = 18;
		
		btnFleets.rect.x = btnRadars.rect.x + 55;
		btnFleets.rect.y = btnRadars.rect.y;
		btnFleets.rect.width = 53;
		btnFleets.rect.height = 18;
		
		btnStars.rect.x = btnRadars.rect.x;
		btnStars.rect.y = btnRadars.rect.y + 20;
		btnStars.rect.width = 53;
		btnStars.rect.height = 18;
		
		btnGrids.rect.x = btnFleets.rect.x;
		btnGrids.rect.y = btnStars.rect.y;
		btnGrids.rect.width = 53;
		btnGrids.rect.height = 18;
		
		btnName.rect.x = btnStars.rect.x;
		btnName.rect.y = btnStars.rect.y + 20;
		btnName.rect.width = 108;
		btnName.rect.height = 18;
		
		btnMove.rect.x = btnStars.rect.x - 105;
		btnMove.rect.y = btnColonize.rect.y + 1;
		btnMove.rect.width = 98;
		btnMove.rect.height = 23;
		
		btnAttack.rect.x = btnMove.rect.x;
		btnAttack.rect.y = btnMove.rect.y + 27;
		btnAttack.rect.width = 98;
		btnAttack.rect.height = 23;
		
		btnStop.rect.x = btnMove.rect.x;
		btnStop.rect.y = btnAttack.rect.y + 27;
		btnStop.rect.width = 98;
		btnStop.rect.height = 23;
		
		updateSatellites();
	}
	/** Update satellite button. */
	public void updateSatellites() {
		int x = btnColonize.rect.x - 91;
		int y = btnColonize.rect.y;
		if (btnSatellite.visible) {
			btnSatellite.rect.x = x;
			btnSatellite.rect.y = y;
			btnSatellite.rect.width = 84;
			btnSatellite.rect.height = 17;
			y += 20;
		}
		if (btnSpySat1.visible) {
			btnSpySat1.rect.x = x;
			btnSpySat1.rect.y = y;
			btnSpySat1.rect.width = 84;
			btnSpySat1.rect.height = 17;
			y += 20;
		}
		if (btnSpySat2.visible) {
			btnSpySat2.rect.x = x;
			btnSpySat2.rect.y = y;
			btnSpySat2.rect.width = 84;
			btnSpySat2.rect.height = 17;
			y += 20;
		}
		if (btnHubble2.visible) {
			btnHubble2.rect.x = x;
			btnHubble2.rect.y = y;
			btnHubble2.rect.width = 84;
			btnHubble2.rect.height = 17;
			y += 20;
		}
	}
	/** Update the location records for the scrollbar knobs. */
	public void updateScrollKnobs() {
		int hextsize = Math.max(gfx.contents.hscrollLeft.getWidth() + gfx.contents.hscrollRight.getWidth(), hscrollRect.width - hscrollMax);

		hknobRect.x = hscrollRect.x + hscrollValue;
		hknobRect.width = hextsize;
		hknobRect.y = hscrollRect.y;
		hknobRect.height = gfx.contents.hscrollLeft.getHeight();

		int vextsize = Math.max(gfx.contents.vscrollTop.getHeight() + gfx.contents.hscrollRight.getHeight(), vscrollRect.height - vscrollMax);
		
		vknobRect.x = vscrollRect.x;
		vknobRect.width = gfx.contents.vscrollTop.getWidth();
		vknobRect.y = vscrollRect.y  + vscrollValue;
		vknobRect.height = vextsize;
		
		
	}
	/** Zoom in or out on a particular region. */
	public void zoom(float newZoomFactor) {
		if (newZoomFactor > 1.0f) {
			newZoomFactor = 1.0f;
		}
		this.zoomFactor = newZoomFactor;
		// ************************************************************
		// HORIZONTAL SCROLLBAR CORRECTION
		// determine the maximum values for the horizontal scrollbar
		int zoomedWidth = (int)(gfx.contents.fullMap.getWidth() * newZoomFactor);
		// get the number of pixels differring in the size of the map and the scrollbar region
		// minus the smallest scrollbar knob
		int minKnobSize = gfx.contents.hscrollLeft.getWidth() + gfx.contents.hscrollRight.getWidth();
		int maxScrollRegion = hscrollRect.width - minKnobSize;
		int mapExcess = Math.max(zoomedWidth - mapRect.width, 0);
		// if the excess area is smaller than the the maxScrollRegion, lets grow the knob
		// with the difference
		if (mapExcess < maxScrollRegion) {
			hscrollMax = Math.max(mapExcess, 0);
			hscrollFactor = 1.0f;
		} else {
			// otherwise just hold it on the max scroll region size.
			hscrollMax = maxScrollRegion;
			hscrollFactor = mapExcess * 1.0f / maxScrollRegion;
		}
		// compensate the scroll value
		// ************************************************************
		// VERTICAL SCROLLBAR CORRECTION
		int zoomedHeight = (int)(gfx.contents.fullMap.getHeight() * newZoomFactor);
		minKnobSize = gfx.contents.vscrollTop.getHeight() + gfx.contents.vscrollBottom.getHeight();
		maxScrollRegion = vscrollRect.height - minKnobSize;
		mapExcess = Math.max(zoomedHeight - mapRect.height, 0);
		if (mapExcess < maxScrollRegion) {
			vscrollMax = Math.max(mapExcess, 0);
			vscrollFactor = 1.0f;
		} else {
			vscrollMax = maxScrollRegion;
			vscrollFactor = mapExcess * 1.0f / maxScrollRegion;
		}

		scroll(hscrollValue, vscrollValue);
	}
	/** Scroll to a particular scrollbar position. */
	public void scroll(int xValue, int yValue) {
		hscrollValue = Math.max(Math.min(xValue, hscrollMax), 0);
		vscrollValue = Math.max(Math.min(yValue, vscrollMax), 0);
		// calculate map offset based on the current scrollbar locations
//			mapX = (int)(hscrollValue * hscrollFactor);
//			mapY = (int)(vscrollValue * vscrollFactor);
		updateScrollKnobs();
		repaint();
	}
	/** Scroll the map by relative coordinates given in pixels. */
	public void scrollByPixelRel(int dx, int dy) {
		scroll((int)(hscrollValue + dx / zoomFactor / hscrollFactor), 
				(int)(vscrollValue + dy / zoomFactor / vscrollFactor));
	}
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		int dx = lastMouseX - e.getX();
		int dy = lastMouseY - e.getY();
		lastMouseX = e.getX();
		lastMouseY = e.getY();
		if (mapDragMode) {
			scrollByPixelRel(dx, dy);
		} else
		if (verticalDragMode) {
			scroll(hscrollValue, vscrollValue - dy);
		} else
		if (horizontalDragMode) {
			scroll(hscrollValue - dx, vscrollValue);
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		Point pt = e.getPoint();
		if ((e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) 
				&& !btnMagnify.disabled && btnMagnify.rect.contains(pt)) {
			btnMagnify.click();
		} else {
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.click();
						break;
					}
				}
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON3 && mapRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			mapDragMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON1 && hknobRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			horizontalDragMode = true;
		} else
		if (e.getButton() == MouseEvent.BUTTON1 && vknobRect.contains(pt)) {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			verticalDragMode = true;
		} else 
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (!btnMagnify.disabled && btnMagnify.rect.contains(pt)) {
				magnifyDirection = true;
				btnMagnify.down = true;
				repaint(btnMagnify.rect);
			} else {
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
						break;
					}
				}
				for (Btn b : toggleButtons) {
					if (b.test(pt)) {
						b.down = !b.down;
						repaint(b.rect);
						b.click();
						break;
					}
				}
			}
		} else
		if (e.getButton() == MouseEvent.BUTTON3 && btnMagnify.test(pt)) {
			magnifyDirection = false;
			btnMagnify.down = true;
			repaint(btnMagnify.rect);
		}
	}
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// if the user released the third button, cancel map drag mode
		mapDragMode = false;
		verticalDragMode = false;
		horizontalDragMode = false;
		boolean needRepaint = false;
		for (Btn b : buttons) {
			needRepaint |= b.down;
			b.down = false;
		}
		needRepaint |= btnMagnify.down;
		btnMagnify.down = false;
		if (needRepaint) {
			repaint();
		}
	}
	/** Initialize button actions. */
	private void initActions() {
		buttons.add(btnColony = new Btn());
		buttons.add(btnColonyPrev = new Btn());
		buttons.add(btnColonyNext = new Btn());
		buttons.add(btnEquipment = new Btn());
		buttons.add(btnEquipmentPrev = new Btn());
		buttons.add(btnEquipmentNext = new Btn());
		buttons.add(btnInfo = new Btn());
		buttons.add(btnBridge = new Btn());
		buttons.add(btnColonize = new Btn());
		buttons.add(btnName = new Btn(new BtnAction() {
			@Override
			public void invoke() {
				doNameChange();
			}
		}));
		btnMagnify = new Btn(new BtnAction() {
			@Override
			public void invoke() {
				doMagnify();
			}
		});
		toggleButtons.add(btnFleets = new Btn());
		toggleButtons.add(btnGrids = new Btn());
		toggleButtons.add(btnRadars = new Btn());
		toggleButtons.add(btnStars = new Btn());
		
		toggleButtons.add(btnMove = new Btn());
		toggleButtons.add(btnAttack = new Btn());
		toggleButtons.add(btnStop = new Btn());
		
		buttons.add(btnSatellite = new Btn());
		buttons.add(btnSpySat1 = new Btn());
		buttons.add(btnSpySat2 = new Btn());
		buttons.add(btnHubble2 = new Btn());
		
		btnSatellite.visible = true;
		btnSpySat1.visible = true;
		btnSpySat2.visible = true;
		btnHubble2.visible = true;
		
	}
	private void doMagnify() {
		if (magnifyDirection && magnifyIndex < magnifyFactors.length - 1) {
			magnifyIndex++;
			zoom(magnifyFactors[magnifyIndex] / 30f);
		} else
		if (!magnifyDirection && magnifyIndex > 0) {
			magnifyIndex--;
			zoom(magnifyFactors[magnifyIndex] / 30f);
		}
	}
	private void doNameChange() {
		nameMode = (nameMode + 1) % 4;
		repaint(btnName.rect);
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Point pt = e.getPoint();
		if (mapRect.contains(pt)) {
			if (e.isControlDown()) {
				// zoom in or out
				if (e.getWheelRotation() < 0 && magnifyIndex < magnifyFactors.length - 1) {
					magnifyIndex++;
					zoom(magnifyFactors[magnifyIndex] / 30f);
				} else 
				if (e.getWheelRotation() > 0 && magnifyIndex > 0) {
					magnifyIndex--;
					zoom(magnifyFactors[magnifyIndex] / 30f);
				}
			} else
			if (e.isShiftDown()) {
				// scroll horizontally
				if (e.getWheelRotation() < 0) {
					scrollByPixelRel(-SCROLL_AMOUNT, 0);
				} else {
					scrollByPixelRel(SCROLL_AMOUNT, 0);
				}
			} else {
				// scroll vertically
				if (e.getWheelRotation() < 0) {
					scrollByPixelRel(0, -SCROLL_AMOUNT);
				} else {
					scrollByPixelRel(0, SCROLL_AMOUNT);
				}
			}
		}
	}
}