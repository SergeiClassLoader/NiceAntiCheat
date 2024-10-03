package pro.cyrent.anticheat.util.task;

import pro.cyrent.anticheat.util.Callback;

import java.util.LinkedList;

public class TaskData {
   private final int id;
   private long timestamp;
   private final LinkedList<LumosTask> tasks = new LinkedList<>();

   public TaskData(int id, long timestamp) {
      this.id = id;
      this.timestamp = timestamp;
   }

   public TaskData(int id, Callback<Integer> callback) {
      this.id = id;
      this.addTask(callback);
   }

   public void addTask(Callback<Integer> callback) {
      this.tasks.add(new LumosTask(callback, this.id));
   }

   public void addTask(LumosTask lumosTask) {
      lumosTask.setId(this.id);
      this.tasks.add(lumosTask);
   }

   public void consumeTask() {
      this.tasks.forEach(LumosTask::runTask);
   }

   public boolean hasTask() {
      return this.tasks.size() > 0;
   }

   public int getId() {
      return this.id;
   }

   public long getTimestamp() {
      return this.timestamp;
   }
}