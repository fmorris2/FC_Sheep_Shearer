package scripts.fc.missions.fcsheepshearer.tasks;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSTile;

import scripts.fc.api.abc.ABC2Reaction;
import scripts.fc.api.generic.FCConditions;
import scripts.fc.api.interaction.impl.objects.ClickObject;
import scripts.fc.api.travel.FCTeleporting;
import scripts.fc.api.travel.Travel;
import scripts.fc.framework.data.Vars;
import scripts.fc.framework.task.Task;
import scripts.fc.missions.fcsheepshearer.FCSheepShearer;
import scripts.fc.missions.fcsheepshearer.data.QuestStage;

public class SpinWool extends Task
{
	private static final long serialVersionUID = 8650835682869360935L;
	
	private final RSArea KITCHEN_AREA = new RSArea(new RSTile(3205, 3217, 0), new RSTile(3212, 3211, 0));	
	private final Positionable WHEEL_TILE = new RSTile(3209, 3213, 1);
	private final boolean SHOULD_TELEPORT = General.random(0, 1) == 0;
	private final int INTERFACE_MASTER = 270;
	private final int INTERFACE_CHILD = 14;
	private final int ANIMATION_ID = 894;
	private final long ANIMATION_TIMEOUT = 2400;
	private final int ESTIMATED_SPIN_WAIT = 40000;
	
	private long lastAnimation;
	private ABC2Reaction reaction;
	
	public SpinWool()
	{
		super();
		reaction = new ABC2Reaction(false, ESTIMATED_SPIN_WAIT);
		Vars.get().add("spinReaction", reaction);
		
	}
	
	@Override
	public boolean execute()
	{
		if(KITCHEN_AREA.contains(Player.getPosition()))
		{
			General.println("Failsafe out of kitchen");
			if(new ClickObject("Climb-up", "Staircase", 15).execute())
				Timing.waitCondition(FCConditions.planeChanged(0), 3500);
		}
		else if(!Combat.isUnderAttack() && ShearSheep.SHEEP_PEN.contains(Player.getPosition()) 
				&& (!SHOULD_TELEPORT || !FCTeleporting.homeTeleport()))
			Travel.webWalkTo(WHEEL_TILE);
		else if(Player.getPosition().distanceTo(WHEEL_TILE) > 3)
		{
			GameTab.open(TABS.INVENTORY);
			Travel.webWalkTo(WHEEL_TILE);
		}
		else
			spinWool();
		
		return false;
	}

	@Override
	public boolean shouldExecute()
	{
		return Game.getSetting(FCSheepShearer.QUEST_SETTING_INDEX) == QuestStage.STARTED.getSetting()
				&& Inventory.getCount("Ball of wool") + Inventory.getCount("Wool") >= 20 &&
				Inventory.getCount("Ball of wool") < 20;
	}

	@Override
	public String getStatus()
	{
		return "Spin wool";
	}
	
	private void spinWool()
	{
		if(Timing.timeFromMark(lastAnimation) > ANIMATION_TIMEOUT) //not spinning
		{
			handleWheel();
		}
		else //spinning
		{
			reaction.start();
			if(Player.getAnimation() == ANIMATION_ID)
				lastAnimation = Timing.currentTimeMillis();
		}
	}
	
	private void handleWheel()
	{
		RSInterface inter = Interfaces.get(INTERFACE_MASTER, INTERFACE_CHILD);
		
		if(inter == null || inter.isHidden())
		{
			ClickObject clickWheel = new ClickObject("Spin", "Spinning wheel", 6);
			clickWheel.setCheckPath(true);
			if(clickWheel.execute())
				Timing.waitCondition(FCConditions.interfaceUp(INTERFACE_MASTER), 3000);
		}
		else if(Clicking.click(inter) && Timing.waitCondition(FCConditions.animationChanged(-1), 2400))
		{
			lastAnimation = Timing.currentTimeMillis();
		}
	}

}
