package sample.distribute;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;

public class TaskExecuter extends UntypedActor {

	private ActorRef router;
	private ActorRef master;
	private Task currentTask;

	public TaskExecuter() {
	}

	@Override
	public void preStart() throws Exception {
		router = getActorRef(Worker.class, "/user/router1", "router1", "router-dispatcher");
		
		context().setReceiveTimeout(Duration.create(60, TimeUnit.SECONDS));
		
		context().system().eventStream().subscribe(self(), DeadLetter.class);
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if (arg0 instanceof Task) {
			currentTask = (Task) arg0;
			this.master = sender();
			router.tell(currentTask, sender()); // send TaskDone message directly to master

		} else if (arg0 instanceof ReceiveTimeout) {
			this.master.tell(new RetryTask(currentTask), getSelf());  // refresh master
			System.out.println("[ReceiveTimeout] Retry send message: " + currentTask);

		} else if (arg0 instanceof DeadLetter) {
			if (((DeadLetter) arg0).message() == currentTask) {
				context().system().scheduler().scheduleOnce(Duration.create(1000, TimeUnit.MILLISECONDS), new Runnable() {
					@Override
					public void run() {
						master.tell(new RetryTask(currentTask), getSelf());  // refresh master
						System.out.println("[DeadLetter] Retry send message: " + currentTask);
					}
				}, context().dispatcher());
			}
		}
	}

	/**
	 * 
	 */
	protected ActorRef getActorRef(Class<?> clazz, String actorPath, String actorName, String dispatcher) {
		try {
			ActorSelection actorSelection = context().system().actorSelection(actorPath);
			return Await.result(actorSelection.resolveOne(Duration.create(10000, TimeUnit.MILLISECONDS)),
					Duration.create(10000, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			try {
				return context().system().actorOf(Props.create(clazz).withDispatcher(dispatcher), actorName);
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
				return null;
			}
		}
	}

}
