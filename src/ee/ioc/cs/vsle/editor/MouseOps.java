package ee.ioc.cs.vsle.editor;

import ee.ioc.cs.vsle.util.*;
import ee.ioc.cs.vsle.vclass.*;
import ee.ioc.cs.vsle.graphics.Shape;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Mouse operations on Canvas.
 */
class MouseOps
	extends MouseInputAdapter {

	ArrayList<GObj> selectedObjs = new ObjectList();
	Canvas canvas;
	public Point draggedBreakPoint;
	public String state = State.selection;
	int cornerClicked;
	int startX, startY;
    boolean mouseOver;

	public MouseOps(Canvas e) {
		this.canvas = e;
	}

	public void setState(String state) {
		this.state = state;
		
		canvas.firstPort = null;
		canvas.currentCon = null;
		canvas.currentObj = null;
        
        if (State.addRelation.equals(state))
            canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        else if (State.selection.equals(state)) {
            canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            canvas.palette.resetButtons();
        } else if (State.magnifier.equals(state))
            canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void addObj() {
		canvas.objects.add(canvas.currentObj);
		canvas.currentObj = null;
	}

	/**
	 * Mouse entered event from the MouseMotionListener. Invoked when the mouse enters a component.
	 * @param e MouseEvent - Mouse event performed.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		mouseOver = true;
	}

	/**
	 * Mouse exited event from the MouseMotionListener. Invoked when the mouse exits a component.
	 * @param e MouseEvent - Mouse event performed.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
        mouseOver = false;
        canvas.drawingArea.repaint();
	}

	private void openObjectPopupMenu(int x, int y) {
		ObjectPopupMenu popupMenu = new ObjectPopupMenu(canvas);

		if (canvas.currentObj.className == null) {
			popupMenu.remove(popupMenu.itemViewCode);
		} else {
			popupMenu.remove(popupMenu.itemMakeClass);
		}
		popupMenu.show(canvas, x, y);



		if (canvas.objects.getSelected().size() < 2) {
			if (canvas.currentObj.isGroup()) {
				popupMenu.enableDisableMenuItem(popupMenu.itemUngroup, true);
			} else {
				popupMenu.enableDisableMenuItem(popupMenu.itemUngroup, false);
			}
			popupMenu.enableDisableMenuItem(popupMenu.itemProperties, true);
			popupMenu.enableDisableMenuItem(popupMenu.itemGroup, false);

			// Enable or disable order changing menu items.
			if (canvas.objects.indexOf(canvas.currentObj) == canvas.objects.size() - 1) {
				popupMenu.enableDisableMenuItem(popupMenu.itemForward, false);
				popupMenu.enableDisableMenuItem(popupMenu.itemToFront, false);
			} else {
				popupMenu.enableDisableMenuItem(popupMenu.itemForward, true);
				popupMenu.enableDisableMenuItem(popupMenu.itemToFront, true);
			}

			if (canvas.objects.indexOf(canvas.currentObj) == 0) {
				popupMenu.enableDisableMenuItem(popupMenu.itemBackward, false);
				popupMenu.enableDisableMenuItem(popupMenu.itemToBack, false);
			} else {
				popupMenu.enableDisableMenuItem(popupMenu.itemBackward, true);
				popupMenu.enableDisableMenuItem(popupMenu.itemToBack, true);
			}

		} else {
			popupMenu.enableDisableMenuItem(popupMenu.itemBackward, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemForward, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemToFront, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemToBack, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemGroup, true);
			popupMenu.enableDisableMenuItem(popupMenu.itemUngroup, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemProperties, false);
			popupMenu.enableDisableMenuItem(popupMenu.itemGroup, true);
			popupMenu.enableDisableMenuItem(popupMenu.itemUngroup, false);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x, y;

		// ignore mouse clicks while dragging
        if (State.drag.equals(state))
        	return;

		x = e.getX();
		y = e.getY();

		if (SwingUtilities.isRightMouseButton(e)) {
            if (State.selection.equals(state)) {
                Connection relation = canvas.connections.nearPoint(x, y);
                if (relation != null) {
                    ConnectionPopupMenu popupMenu = new ConnectionPopupMenu(relation, canvas.connections, canvas);
                    popupMenu.show(canvas, x + canvas.drawingArea.getX(), y + canvas.drawingArea.getY());
                } else {
                    canvas.currentObj = canvas.objects.checkInside(x, y);
                    if (canvas.currentObj != null) {
                        openObjectPopupMenu(x + canvas.drawingArea.getX(), y + canvas.drawingArea.getY());
                    }
                }
            } else if (State.magnifier.equals(state)) {
				canvas.drawAreaSize.width = (int) (canvas.drawAreaSize.width * 0.8);
				canvas.drawAreaSize.height = (int) (canvas.drawAreaSize.height * 0.8);
				canvas.drawingArea.setPreferredSize(canvas.drawAreaSize);
				canvas.objects.updateSize(0.8f, 0.8f);
				canvas.drawingArea.revalidate();
				canvas.connections.calcAllBreakPoints();
                canvas.setScale(canvas.getScale() * 0.8f);
			} else if (State.addRelation.equals(state)) {
				// remove last breakpoint or stop adding the relation
				// when only the first breakpoints is left
				if (canvas.currentCon != null) {
					ArrayList<Point> bps = canvas.currentCon.breakPoints;
					if (bps != null && bps.size() > 1)
						bps.remove(bps.size() - 1);
					else
						canvas.stopRelationAdding();
				}
			} else {
                setState(State.selection);
                // if adding relation class and first port was connected
                if (canvas.firstPort != null) {
                    if (canvas.firstPort.getConnections() == null
                            || canvas.firstPort.getConnections().size() == 0)
                        canvas.firstPort.setConnected(false);

                    canvas.firstPort = null;
                }
            }
		} // **********End of RIGHT mouse button controls**********************************************
		else {

			// **********Magnifier	Code************************
			if (state.equals(State.magnifier)) {
				canvas.drawAreaSize.width = (int) (canvas.drawAreaSize.width * 1.25);
				canvas.drawAreaSize.height = (int) (canvas.drawAreaSize.height * 1.25);
				canvas.drawingArea.setPreferredSize(canvas.drawAreaSize);
				canvas.drawingArea.revalidate();
				canvas.objects.updateSize(1.25f, 1.25f);
				canvas.connections.calcAllBreakPoints();
				canvas.setScale(canvas.getScale() * 1.25f);
			}

			// **********Relation adding code**************************
			if (state.equals(State.addRelation)) {
				GObj obj = canvas.objects.checkInside(x, y);

				if (obj != null) {
					Port port = obj.portContains(x, y);

					if (port != null) {
						if (canvas.firstPort == null) {
							canvas.firstPort = port;
							canvas.firstPort.setConnected(true);
							canvas.currentCon = new Connection();
							canvas.currentCon.addBreakPoint(new Point(canvas.firstPort.getX() + canvas.firstPort.getObject().getX(), canvas.firstPort.getY() + canvas.firstPort.getObject().getY()));
							canvas.mouseX = x;
							canvas.mouseY = y;
						} else if (canBeConnected(canvas.firstPort, port)) {

							if (port == canvas.firstPort) {
								canvas.firstPort.setConnected(false);
								canvas.firstPort = null;
								canvas.currentCon = null;
							} else {
								port.setConnected(true);
								canvas.currentCon.beginPort = canvas.firstPort;
								canvas.currentCon.endPort = port;
								canvas.firstPort.addConnection(canvas.currentCon);
								port.addConnection(canvas.currentCon);
								canvas.currentCon.addBreakPoint(new Point(port.getX() + port.getObject().getX(), port.getY() + port.getObject().getY()));
								canvas.connections.add(canvas.currentCon);
								canvas.firstPort = null;
							}
						}
					}
				} else {
					if (canvas.firstPort != null) {
						if (e.getClickCount() == 2) {
							canvas.stopRelationAdding();
						} else
							canvas.currentCon.addBreakPoint(new Point(x, y));
						// firstPort.setConnected(false);
						// firstPort=null;
					}
				}

			} // **********Selecting objects code*********************
			else if (state.equals(State.selection)) {
				Connection con = canvas.connections.nearPoint(x, y);

				if (con != null) {
					con.selected = true;
				} else {
					canvas.connections.clearSelected();
				}
				GObj obj = canvas.objects.checkInside(x, y);

				if (obj == null) {
					canvas.objects.clearSelected();
				} else {
					if (!e.isShiftDown()) {
						canvas.objects.clearSelected();
						obj.setSelected(true);
					}
				}

			} else {
				if (state.startsWith("??")) { // if class is of type relation
					addingSpecialRelation(y, x);
				} else if (canvas.currentObj != null) {
					addObj();
					setState(State.selection);
				}
			}
		}
		canvas.drawingArea.repaint();
	}

	private void addingSpecialRelation(int y, int x) {
		GObj obj = canvas.objects.checkInside(x, y);
		if (obj != null) {
			Port port = obj.portContains(x, y);
			if (port != null) {
                GObj currentObj = canvas.currentObj;
                if (currentObj == null || currentObj.ports == null
                        || currentObj.ports.size() != 2)
                    return;
                
                if (canvas.firstPort == null) {
                    if (!canBeConnected(port, currentObj.ports.get(0)))
                        return;
					canvas.firstPort = port;
					canvas.firstPort.setConnected(true);
					canvas.mouseX = x;
					canvas.mouseY = y;
				} else {
                    if (!canBeConnected(port, currentObj.ports.get(1)))
                        return;
					Port port1 = canvas.currentObj.ports.get(0);
					Port port2 = canvas.currentObj.ports.get(1);
					Connection con = new Connection(canvas.firstPort, port1);
					canvas.firstPort.addConnection(con);
					port1.addConnection(con);
					canvas.connections.add(con);
					con = new Connection(port2, port);
					port2.addConnection(con);
					port.addConnection(con);
					canvas.connections.add(con);
					port.setConnected(true);
					RelObj thisObj = (RelObj) canvas.currentObj;
					thisObj.startPort = canvas.firstPort;
					thisObj.endPort = port;
					canvas.firstPort = null;
					addObj();
					startAddingObject();
					//setState(State.selection);
					canvas.objects.updateRelObjs();
					//editor.currentObj = null;

				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (state.equals(State.selection)) {
			canvas.mouseX = e.getX();
			canvas.mouseY = e.getY();
			Connection con = canvas.connections.nearPoint(canvas.mouseX, canvas.mouseY);
			if (con != null) {
				draggedBreakPoint = con.breakPointContains(canvas.mouseX, canvas.mouseY);
			}
			if (con != null && draggedBreakPoint != null) {
				setState(State.dragBreakPoint);
			} else {
				GObj obj = canvas.objects.checkInside(canvas.mouseX, canvas.mouseY);
				if (obj != null) {
					if (e.isShiftDown()) {
						obj.setSelected(true);
					} else {
						if (!obj.isSelected()) {
							canvas.objects.clearSelected();
							obj.setSelected(true);
						}
					}
                    if (SwingUtilities.isLeftMouseButton(e))
                        setState(State.drag);
					canvas.drawingArea.repaint();
				} else {
					cornerClicked = canvas.objects.controlRectContains(canvas.mouseX, canvas.mouseY);
					if (cornerClicked != 0) {
						//if (obj.isSelected())
						setState(State.resize);
					} else {
						setState(State.dragBox);
						startX = canvas.mouseX;
						startY = canvas.mouseY;
					}
				}
				// drawConnections();
				selectedObjs = canvas.objects.getSelected();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if( !SwingUtilities.isLeftMouseButton(e) ) {
			return;
		}
		int x = e.getX();
		int y = e.getY();
		GObj obj;
		Connection relation;

        canvas.setPosInfo(x, y);

		if (state.equals(State.dragBreakPoint)) {
			draggedBreakPoint.x = x;
			draggedBreakPoint.y = y;
			canvas.drawingArea.repaint();
		}
		if (state.equals(State.drag)) {
			int x1, x2, y1, y2, newX, newY;
			for (int i = 0; i < selectedObjs.size(); i++) {
				obj = selectedObjs.get(i);
				if (!(obj instanceof RelObj)) {

					if (RuntimeProperties.snapToGrid == 1) {
						//use the following when  snap to grid
						x1 = Math.round(x / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						x2 = Math.round(canvas.mouseX / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						y1 = Math.round(y / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						y2 = Math.round(canvas.mouseY / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						newX = Math.round((obj.getX() + (x1 - x2)) / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						newY = Math.round((obj.getY() + (y1 - y2)) / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
						obj.setPosition(newX, newY);
					} else {
						//use this when not snap to grid
						obj.setPosition(obj.getX() + (x - canvas.mouseX), obj.getY() + (y - canvas.mouseY));
					}
				}

				// check if a strict port exists on the object
				if (obj.isStrict()) {
					Port port, port2;
					GObj obj2;
					ArrayList<Port> ports = obj.getPorts();

					for (int j = 0; j < ports.size(); j++) {
						port = ports.get(j);
						if (port.isStrict()) {
							port2 = port.getStrictConnected();
							// if the port is connected to another port, and they are not both selected, we might
							// wanna remove the connection
							if (port2 != null && !port2.getObject().isSelected()) {
								// We dont want to remove the connection, if the objects belong to the same group
								if (!(obj.isGroup() && obj.includesObject(port2.getObject()))) {
									if (Math.abs(port.getRealCenterX() - port2.getRealCenterX()) > 1 || Math.abs(port.getRealCenterY() - port2.getRealCenterY()) > 1) {
										canvas.connections.remove(port, port2);
									}
								}
							}

							obj2 = canvas.objects.checkInside(port.getObject().getX() + (x - canvas.mouseX) + port.getCenterX(), port.getObject().getY() + (y - canvas.mouseY) + port.getCenterY(), obj);
							if (obj2 != null && !obj2.isSelected()) {
								port2 = obj2.portContains(port.getObject().getX() + (x - canvas.mouseX) + port.getCenterX(), port.getObject().getY() + (y - canvas.mouseY) + port.getCenterY());

								if (port2 != null && port2.isStrict()) {
									if (!port.isConnected()) {
										port.setConnected(true);
										port2.setConnected(true);
										Connection con = new Connection(port, port2);

										port2.addConnection(con);
										port.addConnection(con);
										canvas.connections.add(con);
									}
									obj.setPosition(port2.getObject().x + port2.getCenterX() - ((port.getObject().x - obj.x) + port.getCenterX()), port2.getObject().y + port2.getCenterY() - ((port.getObject().y - obj.y) + port.getCenterY()));
								}
							}
						}
					}
				}

				for (int j = 0; j < canvas.connections.size(); j++) {
					relation = canvas.connections.get(j);
					if (selectedObjs.contains(relation.endPort.getObject()) && selectedObjs.contains(relation.beginPort.getObject())) {
						relation.calcAllBreakPoints();
					} else if (obj.includesObject(relation.endPort.getObject()) || obj.includesObject(relation.beginPort.getObject())) {
						relation.calcEndBreakPoints();
					}

				}


			}
			canvas.objects.updateRelObjs();
			canvas.mouseX = x;
			canvas.mouseY = y;
			canvas.drawingArea.repaint();
		}
		if (state.equals(State.dragBox)) {
			canvas.mouseX = x;
			canvas.mouseY = y;
			canvas.drawingArea.repaint();
		}
		if (state.equals(State.resize)) {
			for (int i = 0; i < selectedObjs.size(); i++) {
				obj = selectedObjs.get(i);

				obj.resize(x - canvas.mouseX, y - canvas.mouseY, cornerClicked);

				for (int j = 0; j < canvas.connections.size(); j++) {
					relation = canvas.connections.get(j);
					if (obj.includesObject(relation.endPort.getObject()) || obj.includesObject(relation.beginPort.getObject())) {
						relation.calcAllBreakPoints();
					}
				}
			}
			canvas.mouseX = x;
			canvas.mouseY = y;
			canvas.drawingArea.repaint();

			canvas.objects.updateRelObjs();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		canvas.setPosInfo(x, y);

		// check if port needs to be nicely drawn coz of mouseover
		if (state.equals(State.addRelation) || state.startsWith("??")) {
			if (canvas.currentPort != null) {
				canvas.currentPort.setSelected(false);
				canvas.drawingArea.repaint();
			}

			GObj obj = canvas.objects.checkInside(x, y);

			if (obj != null) {
				Port port = obj.portContains(x, y);

				if (port != null) {
					if (canvas.firstPort != null) {
						if (canBeConnected(canvas.firstPort, port)) {
							port.setSelected(true);
							canvas.currentPort = port;
							canvas.drawingArea.repaint();
						}
					} else {
						port.setSelected(true);
						canvas.currentPort = port;
						canvas.drawingArea.repaint();
					}
				}
			}
		}

		// if we're adding a new object...
		if (canvas.currentObj != null && canvas.vPackage.hasClass(state)) {
			//use these when snap to grid
			if (RuntimeProperties.snapToGrid == 1) {
				canvas.currentObj.x = Math.round(x / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
				canvas.currentObj.y = Math.round(y / RuntimeProperties.gridStep) * RuntimeProperties.gridStep;
			} else {
				//Use these when not snap to grid:
				canvas.currentObj.y = y;
				canvas.currentObj.x = x;
			}
			// Kui objektil on moni strict port, chekime kas teda kuskile panna on;
			if (canvas.currentObj.isStrict()) {
				Port port, port2;
				GObj obj;

				for (int i = 0; i < canvas.currentObj.ports.size(); i++) {
					port = canvas.currentObj.ports.get(i);
					port2 = port.getStrictConnected();
					if (port2 != null) {
						if (Math.abs(port.getRealCenterX() - port2.getRealCenterX()) > 1 || Math.abs(port.getRealCenterY() - port2.getRealCenterY()) > 1) {
							canvas.connections.remove(port, port2);
						}
					}

					obj = canvas.objects.checkInside(x + port.getCenterX(), y + port.getCenterY());
					if (obj != null) {
						port2 = obj.portContains(x + port.getCenterX(), y + port.getCenterY());

						if (port2 != null && port2.isStrict()) {
							if (!port.isConnected()) {
								port.setConnected(true);
								port2.setConnected(true);
								Connection con = new Connection(port, port2);

								port2.addConnection(con);
								port.addConnection(con);
								canvas.connections.add(con);
							}
							canvas.currentObj.x = port2.getObject().x + port2.getCenterX() - port.getCenterX();
							canvas.currentObj.y = port2.getObject().y + port2.getCenterY() - port.getCenterY();
						}
					}
				}
			}

			Rectangle rect = new Rectangle(x - 10, y - 10, canvas.currentObj.getRealWidth() + 10, canvas.currentObj.getRealHeight() + 10);

			canvas.drawingArea.scrollRectToVisible(rect);
			if (x + canvas.currentObj.getRealWidth() > canvas.drawAreaSize.width) {
				canvas.drawAreaSize.width = x + canvas.currentObj.getRealWidth();
				canvas.drawingArea.setPreferredSize(canvas.drawAreaSize);
				canvas.drawingArea.setPreferredSize(canvas.drawAreaSize);
			}

			if (y + canvas.currentObj.getRealHeight() > canvas.drawAreaSize.height) {
				canvas.drawAreaSize.height = y + canvas.currentObj.getRealHeight();
				canvas.drawingArea.setPreferredSize(canvas.drawAreaSize);
				canvas.drawingArea.revalidate();
			}
			canvas.drawingArea.repaint();
		} else if (state.startsWith("??") && canvas.firstPort != null) { //if class is of type relation
			canvas.drawingArea.repaint();
		}
		if (canvas.firstPort != null) {
			canvas.mouseX = x;
			canvas.mouseY = y;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (state.equals(State.dragBreakPoint)) {
			state = State.selection;
		}
		if (state.equals(State.drag)) {
            if (!SwingUtilities.isLeftMouseButton(e))
                return;
			state = State.selection;
		}
		if (state.equals(State.resize)) {
			state = State.selection;
		}
		if (state.equals(State.dragBox)) {
            int x1 = Math.min(startX, canvas.mouseX);
            int x2 = Math.max(startX, canvas.mouseX);
            int y1 = Math.min(startY, canvas.mouseY);
            int y2 = Math.max(startY, canvas.mouseY);
            canvas.objects.selectObjectsInsideBox(x1, y1, x2, y2);
			state = State.selection;
			canvas.drawingArea.repaint();
		}
		if (canvas.objects.getSelected() != null && canvas.objects.getSelected().size() > 0) {
			String selObjects = canvas.objects.getSelected().toString();

			if (selObjects != null) {
				selObjects = selObjects.replaceAll("null ", " ");
			}
			canvas.posInfo.setText("Selection: " + selObjects);
		}
	}

	private boolean canBeConnected(Port firstPort, Port port) {
		if ( firstPort.isMulti() && port.isMulti() )
			return false;
		else if( ( firstPort.isMulti() && firstPort.getType().equals( port.getType()) ) 
				|| ( port.isMulti() && port.getType().equals( firstPort.getType()) ) ) 
			return true;
		
		else if (firstPort.getType().equals(port.getType()))
			return true;

		else if ((port.isAny() || firstPort.isAny()) && !(port.isAny() || firstPort.isAny())) {
			return true;
		}
		else if ( TypeUtil.TYPE_ALIAS.equals( port.getType() ) && firstPort.getType().substring(firstPort.getType().length() - 2, firstPort.getType().length()).equals("[]"))
			return true;
		else if ( TypeUtil.TYPE_ALIAS.equals( firstPort.getType() ) && port.getType().substring(port.getType().length() - 2, port.getType().length()).equals("[]"))
			return true;
		else 
			return false;
	}

    void startAddingObject() {
        GObj obj;
        PackageClass pClass;

        if (state.startsWith("??")) {
            pClass = canvas.vPackage.getClass(state.substring(2));
            obj = new RelObj(0, 0, pClass.graphics.getWidth(), pClass.graphics.getHeight(), pClass.toString());
        } else {
            pClass = canvas.vPackage.getClass(state);
            obj = new GObj(0, 0, pClass.graphics.getWidth(), pClass.graphics.getHeight(), pClass.toString());
        }

        if (pClass.painterPrototype != null) {
            ClassPainter painter = pClass.painterPrototype.clone();
            painter.setClass(obj);
            painter.setScheme(canvas.scheme);
            canvas.classPainters.put(obj, painter);
        }

        obj.shapes = new ArrayList<Shape>(pClass.graphics.shapes.size());
        for (Shape shape: pClass.graphics.shapes)
            obj.shapes.add(shape.clone());

        obj.ports = new ArrayList<Port>(pClass.ports.size());
        for (Port port: pClass.ports) {
            port = port.clone();
            port.setObject(obj);
            obj.ports.add(port);

            if (port.isStrict()) {
                obj.strict = true;
            }

            if (port.x + port.getOpenGraphics().boundX < obj.portOffsetX1) {
                obj.portOffsetX1 = port.x + port.getOpenGraphics().boundX;
            }

            if (port.y + port.getOpenGraphics().boundY < obj.portOffsetY1) {
                obj.portOffsetY1 = port.y + port.getOpenGraphics().boundY;
            }

            if (port.x + port.getOpenGraphics().boundWidth > obj.width + obj.portOffsetX2) {
                obj.portOffsetX2 = Math.max((port.x + port.getOpenGraphics().boundX + port.getOpenGraphics().boundWidth) - obj.width, 0);
            }

            if (port.y + port.getOpenGraphics().boundHeight > obj.height + obj.portOffsetY2) {
                obj.portOffsetY2 = Math.max((port.y + port.getOpenGraphics().boundY + port.getOpenGraphics().boundHeight) - obj.height, 0);
            }

            port.setConnections( new ArrayList<Connection>() );
        }
        // deep clone fields list
        obj.fields = new ArrayList<ClassField>(pClass.fields.size());
        for (ClassField field: pClass.fields)
            obj.fields.add(field.clone());

        obj.setName(pClass.name + "_" + Integer.toString(canvas.objCount));
        canvas.objCount++;

        canvas.currentObj = obj;
        Cursor cursor;
        if (state.startsWith("??")) {
            cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        } else {
            cursor = new Cursor(Cursor.HAND_CURSOR);
        }
        canvas.setCursor(cursor);
    }
}
