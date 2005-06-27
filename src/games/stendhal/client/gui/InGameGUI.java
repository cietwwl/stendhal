package games.stendhal.client.gui;

import java.awt.geom.*;
import java.awt.event.*;
import java.awt.*;

import marauroa.common.*;
import marauroa.common.game.*;
import games.stendhal.client.entity.*;
import games.stendhal.client.*;
import games.stendhal.common.*;

import java.util.*;


public class InGameGUI implements MouseListener, MouseMotionListener, KeyListener
  {
  interface InGameAction
    {
    public void onAction(Object... param);
    }
  
  abstract class InGameActionListener implements InGameAction
    {
    abstract public void onAction(Object... param);
    }
    
  static class InGameButton
    {
    private String name;
    private Sprite[] buttons;
    private Rectangle area;
    private InGameAction action;
    private boolean over;
    private boolean enabled;
    
    public InGameButton(String name, Sprite normal, Sprite over, int x, int y)
      {
      buttons=new Sprite[2];
      buttons[0]=normal;
      buttons[1]=over;
      
      area=new Rectangle(x,y,buttons[0].getWidth(),buttons[0].getHeight());
      this.over=false;
      this.action=null;
      this.enabled=true;
      this.name=name;
      }
    
    public String getName()
      {
      return name;
      }
    
    public void setEnabled(boolean enabled)
      {
      this.enabled=enabled;
      }

    public void draw(GameScreen screen)
      {
      if(!enabled) return;
      Sprite button;
      
      if(over)
        {
        button=buttons[1];
        }
      else
        {
        button=buttons[0];
        }
        
      screen.drawInScreen(button,(int)area.getX(),(int)area.getY());
      }
    
    public void addActionListener(InGameAction action)
      {
      this.action=action;
      }

    public boolean onMouseOver(Point2D point)
      {
      if(!enabled) return false;
      if(area.contains(point))
        {
        over=true;
        }
      else
        {
        over=false;
        }
      
      return false;
      }
    
    public boolean clicked(Point2D point)
      {
      if(!enabled) return false;
      if(area.contains(point))
        {
        action.onAction();
        return true;
        }
      
      return false;
      }    
    }

  static class InGameDroppableArea
    {
    private String name;
    private Rectangle area;
    private InGameAction action;
    private boolean enabled;
    
    public InGameDroppableArea(String name,int x, int y, int width, int height)
      {
      this.name=name;
      area=new Rectangle(x,y,width,height);
      this.action=null;
      this.enabled=true;
      }
    
    public String getName()
      {
      return name;
      }
    
    public void setEnabled(boolean enabled)
      {
      this.enabled=enabled;
      }
    
    public void addActionListener(InGameAction action)
      {
      this.action=action;
      }

    public void draw(GameScreen screen)
      {
      Graphics g=screen.expose();
      g.setColor(Color.white);
      g.drawRect((int)area.getX(),(int)area.getY(),(int)area.getWidth(),(int)area.getHeight());      
      }
      
    public boolean isMouseOver(Point2D point)
      {
      if(area.contains(point))
        {
        return true;
        }
      
      return false;
      }
      
    public boolean released(Point2D point, Entity choosenEntity)
      {
      if(!enabled) return false;
      if(area.contains(point))
        {
        action.onAction(choosenEntity,this);
        return true;
        }
      
      return false;
      }    
    }
    
  static class InGameList
    {
    private Rectangle area;
    private String[] list;
    private int choosen;
    private int over;
    private Sprite action_list;
    
    private Sprite render(double x, double y, double mouse_x, double mouse_y)
      {
      int width=70+6;
      int height=6+16*list.length;
      
      area= new Rectangle((int)x,(int)y,width,height);
      GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      Image image = gc.createCompatibleImage(width,height,Transparency.BITMASK);    
      Graphics g=image.getGraphics();

      g.setColor(Color.gray);
      g.fillRect(0,0,width,height);

      g.setColor(Color.black);
      g.drawRect(0,0,width-1,height-1);
      
      g.setColor(Color.yellow);
      int i=0;
      for(String item: list)
        {
        if((mouse_y-y)>16*i && (mouse_y-y)<16*(i+1))
          {
          g.setColor(Color.white);
          g.drawRect(0,16*i,width-1,16);
          g.drawString(item,3,13+16*i);
          g.setColor(Color.yellow);
          over=i;
          }
        else
          {
          g.drawString(item,3,13+16*i);
          }
          
        i++;
        }
      
      return new Sprite(image);
      }
    
    public InGameList(String[] list, double x, double y)
      {
      this.list=list;
      over=-1;
      action_list=render(x,y,-1,-1);      
      }
    
    public void draw(GameScreen screen)
      {
      Point2D translated=screen.translate(new Point((int)area.getX(),(int)area.getY()));      
      screen.draw(action_list,translated.getX(),translated.getY());
      }
    
    public boolean onMouseOver(Point2D point)
      {
      if(area.contains(point) && over!=(point.getY()-area.getY())/16)
        {
        action_list=render(area.getX(),area.getY(),point.getX(),point.getY());      
        return true;
        }
      
      return false;
      }
    
    public boolean clicked(Point2D point)
      {
      if(area.contains(point))
        {
        choosen=(int)((point.getY()-area.getY())/16);
        return true;
        }
      
      return false;
      }
    
    public String choosen()
      {
      return list[choosen];
      }
    }
  
  private InGameList widget;
  private java.util.List<InGameButton> buttons;  
  private java.util.List<InGameDroppableArea> droppableAreas;
  private Entity widgetAssociatedEntity;
  
  private StendhalClient client;
  private GameObjects gameObjects;
  private GameScreen screen;

  private Map<Integer, Object> pressed;
  
  private Sprite inGameInventory;
  private Sprite inGameDevelPoint;
  
  public InGameGUI(StendhalClient client)
    {
    Logger.trace("InGameGUI::(init)","D","OS: "+(System.getProperty("os.name")));
    
    if(System.getProperty("os.name").toLowerCase().contains("linux"))
      {
      try
        {
        // NOTE: X does handle input in a different way of the rest of the world.
        // This fixs the problem.
        Runtime.getRuntime().exec("xset r off");
        Runtime.getRuntime().addShutdownHook(new Thread()
          {
          public void run()
            {
            try
              {
              Runtime.getRuntime().exec("xset r on");
              }
            catch(Exception e)
              {
              System.out.println (e);
              }
            }
          });
        }
      catch(Exception e)
        {
        System.out.println (e);
        }
      }
      
    this.client=client;
    gameObjects=client.getGameObjects();
    screen=GameScreen.get();
    
    pressed=new HashMap<Integer, Object>();
    
    buttons=new java.util.LinkedList<InGameButton>();
    droppableAreas=new java.util.LinkedList<InGameDroppableArea>();
    
    buildGUI();
    }
  
  private void buildGUI()
    {
    SpriteStore st=SpriteStore.get();
    
    InGameButton button=null;
    button=new InGameButton("atk",st.getSprite("data/atk_up.gif"), st.getSprite("data/atk_up_pressed.gif"), 530,84);
    button.addActionListener(new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        RPAction improve=new RPAction();
        improve.put("type","improve");
        improve.put("stat","atk");
        InGameGUI.this.client.send(improve);
        }
      });
    button.setEnabled(false);
    buttons.add(button);
    
    button=new InGameButton("def",st.getSprite("data/def_up.gif"), st.getSprite("data/def_up_pressed.gif"), 530,84+14);
    button.addActionListener(new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        RPAction improve=new RPAction();
        improve.put("type","improve");
        improve.put("stat","def");
        InGameGUI.this.client.send(improve);
        }
      });
    button.setEnabled(false);
    buttons.add(button);

    button=new InGameButton("hp",st.getSprite("data/hp_up.gif"), st.getSprite("data/hp_up_pressed.gif"), 530,84+28);
    button.addActionListener(new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        RPAction improve=new RPAction();
        improve.put("type","improve");
        improve.put("stat","hp");
        InGameGUI.this.client.send(improve);
        }
      });
    button.setEnabled(false);
    buttons.add(button);


    button=new InGameButton("exit",st.getSprite("data/exit.gif"), st.getSprite("data/exit_pressed.gif"), 320,360);
    button.addActionListener(new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        InGameGUI.this.client.requestLogout();
        }
      });
    button.setEnabled(false);
    buttons.add(button);
    
    button=new InGameButton("back",st.getSprite("data/back.gif"), st.getSprite("data/back_pressed.gif"), 220,360);
    button.addActionListener(new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        for(InGameButton button: buttons)    
          {
          if(button.getName().equals("exit") || button.getName().equals("back"))
            {
            button.setEnabled(false);
            }
          } 
        }
      });
    button.setEnabled(false);
    buttons.add(button);
    
    /** Inventory */
    inGameInventory=SpriteStore.get().getSprite("data/equipmentGUI.gif");
    
    InGameActionListener dropToInventory=new InGameActionListener()
      {
      public void onAction(Object... param)
        {
        RPAction action=new RPAction();
        action.put("type","equip");
        action.put("target",((Entity)param[0]).getID().getObjectID());
        action.put("slot",((InGameDroppableArea)param[1]).getName());
        InGameGUI.this.client.send(action);
        }
      };
    
    InGameDroppableArea area=new InGameDroppableArea("lhand",532,54,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    area=new InGameDroppableArea("head",565,12,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    area=new InGameDroppableArea("torso",565,43,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    area=new InGameDroppableArea("legs",565,74,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    area=new InGameDroppableArea("feet",565,105,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    area=new InGameDroppableArea("rhand",598,54,28,28);
    area.addActionListener(dropToInventory);
    droppableAreas.add(area);
    
    }
  
  private MouseEvent lastDraggedEvent;
  private Entity choosenEntity;
  private InGameDroppableArea choosenWidget;
    
  public void mouseDragged(MouseEvent e) 
    {
    lastDraggedEvent=e;
    }
    
  public void mouseMoved(MouseEvent e)  
    {
    lastDraggedEvent=null;
    
    if(widget!=null)
      {
      widget.onMouseOver(e.getPoint());
      }
    
    for(InGameButton button: buttons)    
      {
      button.onMouseOver(e.getPoint());
      } 
    }

  public void mouseClicked(MouseEvent e) 
    {
    Point2D screenPoint=e.getPoint();
        
    if(widget!=null && widget.clicked(screenPoint))
      {
      if(gameObjects.has(widgetAssociatedEntity))
        {
        widgetAssociatedEntity.onAction(client, widget.choosen());
        widget=null;
        return;
        }
      }

    for(InGameButton button: buttons)    
      {
      button.clicked(e.getPoint());
      } 

    widget=null;
    
    Point2D point=screen.translate(screenPoint);
    Entity entity=gameObjects.at(point.getX(),point.getY());
    if(entity!=null)
      {
      if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>1)
        {        
        String action=entity.defaultAction();
        entity.onAction(client, action);
        }
      else if(e.getButton()==MouseEvent.BUTTON3)
        {
        String[] actions=entity.offeredActions();
        widget=new InGameList(actions,screenPoint.getX(),screenPoint.getY());      
        widgetAssociatedEntity=entity;  
        }
      }
    }

  public void mousePressed(MouseEvent e) 
    {
    if(e.getButton()==MouseEvent.BUTTON1)
      {        
      Point2D point=screen.translate(e.getPoint());
      choosenEntity=gameObjects.at(point.getX(),point.getY());
      
      if(choosenEntity==null)
        {
        for(InGameDroppableArea item: droppableAreas)    
          {
          if(item.isMouseOver(e.getPoint()))
            {
            choosenWidget=item;
            return;
            }
          } 
        }
      }
    }

  public void mouseReleased(MouseEvent e) 
    {
    if(lastDraggedEvent!=null && choosenEntity!=null)
      {
      Point2D point=screen.translate(e.getPoint());
      System.out.println (choosenEntity+" moved to "+point);
      
      // We check first inventory and if it fails we wanted to move the object so. 
      for(InGameDroppableArea item: droppableAreas)    
        {
        if(item.released(e.getPoint(),choosenEntity))
          {
          // We dropped it in inventory
          System.out.println ("Dropped "+choosenEntity+" into "+item.getName());
          return;
          }
        }

      choosenEntity.onAction(client, "Displace", Integer.toString((int)point.getX()), Integer.toString((int)point.getY()));
      choosenEntity=null;
      lastDraggedEvent=null;
      }

    if(lastDraggedEvent!=null && choosenWidget!=null)
      {
      Point2D point=screen.translate(e.getPoint());
      System.out.println (choosenWidget.getName()+" dropped to "+point);
      
      RPAction action=new RPAction();
      action.put("type","drop");
      action.put("slot",choosenWidget.getName());
      action.put("x",(int)point.getX());
      action.put("y",(int)point.getY());
      InGameGUI.this.client.send(action);
      }
    }

  public void mouseEntered(MouseEvent e) 
    {
    }

  public void mouseExited(MouseEvent e) 
    {
    }    

  public void onKeyPressed(KeyEvent e)  
    {
    RPAction action;
    
    if(e.getKeyCode()==KeyEvent.VK_L && e.isControlDown())
      {
      client.getGameLogDialog().setVisible(true);
      }
    else if(e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT || e.getKeyCode()==KeyEvent.VK_UP || e.getKeyCode()==KeyEvent.VK_DOWN)
      {
      action=new RPAction();
      if(e.isControlDown())
        {
        action.put("type","face");
        }
      else
        {
        action.put("type","move");
        }
      
      switch(e.getKeyCode())
        {
        case KeyEvent.VK_LEFT:
          action.put("dir",Direction.LEFT.get());
          break;
        case KeyEvent.VK_RIGHT:
          action.put("dir",Direction.RIGHT.get());
          break;
        case KeyEvent.VK_UP:
          action.put("dir",Direction.UP.get());
          break;
        case KeyEvent.VK_DOWN:
          action.put("dir",Direction.DOWN.get());
          break;
        }
      
      client.send(action);
      }
    }
    
  public void onKeyReleased(KeyEvent e)  
    {
    RPAction action=new RPAction();
    action.put("type","move");
    
    switch(e.getKeyCode())
      {
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_UP:
      case KeyEvent.VK_DOWN:   
        int keys=(pressed.containsKey(KeyEvent.VK_LEFT)?1:0)+(pressed.containsKey(KeyEvent.VK_RIGHT)?1:0)+(pressed.containsKey(KeyEvent.VK_UP)?1:0)+(pressed.containsKey(KeyEvent.VK_DOWN)?1:0);   
        if(keys==1)
          {
          action.put("dir",Direction.STOP.get());
          client.send(action);
          }
        break;
      }
    }
    
  public void keyPressed(KeyEvent e) 
    {
    widget=null;
    
    if(!pressed.containsKey(new Integer(e.getKeyCode())))
      {
      onKeyPressed(e);
      pressed.put(new Integer(e.getKeyCode()),null);
      }      
    }
      
  public void keyReleased(KeyEvent e) 
    {
    onKeyReleased(e);
    pressed.remove(new Integer(e.getKeyCode()));
    }

  public void keyTyped(KeyEvent e) 
    {
    if (e.getKeyChar() == 27) 
      {
      RPAction rpaction=new RPAction();
      rpaction.put("type","stop");
      client.send(rpaction);

      for(InGameButton button: buttons)    
        {
        if(button.getName().equals("exit") || button.getName().equals("back"))
          {
          button.setEnabled(true);
          }
        } 
      }
    }

  public void draw(GameScreen screen)
    {
    screen.drawInScreen(inGameInventory,530,10);

    for(InGameDroppableArea item: droppableAreas)    
      {
      item.draw(screen);
      } 
    
    RPObject player=client.getPlayer();
    if(player!=null)
      {
      if(player.hasSlot("lhand"))
        {
        RPSlot slot=player.getSlot("lhand");
        if(slot.size()==1)
          {
          RPObject object=slot.iterator().next();
          screen.drawInScreen(gameObjects.spriteType(object),532,54);
          }
        }
      
      if(player.hasSlot("rhand"))
        {
        RPSlot slot=player.getSlot("rhand");
        if(slot.size()==1)
          {
          RPObject object=slot.iterator().next();
          screen.drawInScreen(gameObjects.spriteType(object),598,54);
          }
        }
      
      screen.drawInScreen(screen.createString("HP : "+player.get("hp")+"/"+player.get("base_hp"),Color.white),550, 144);
      screen.drawInScreen(screen.createString("ATK: "+player.get("atk"),Color.white),550, 164);
      screen.drawInScreen(screen.createString("DEF: "+player.get("def"),Color.white),550, 184);
      screen.drawInScreen(screen.createString("XP : "+player.get("xp"),Color.white),550, 204);
      
      if(player.has("devel") && player.getInt("devel")>0)
        {
        screen.drawInScreen(screen.createString("Devel: "+player.get("devel"),Color.yellow),550, 224);
        
        for(InGameButton button: buttons)    
          {
          if(button.getName().equals("hp") || button.getName().equals("atk") || button.getName().equals("def"))
            {
            button.setEnabled(true);
            }
          }
        }
      else
        {
        for(InGameButton button: buttons)    
          {
          if(button.getName().equals("hp") || button.getName().equals("atk") || button.getName().equals("def"))
            {
            button.setEnabled(false);
            }
          }
        }
      }
    
    for(InGameButton button: buttons)    
      {
      button.draw(screen);
      } 
    
    if(widget!=null)
      {
      widget.draw(screen);
      }
    }
  }
