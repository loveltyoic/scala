package example.producer

import java.util.Properties

import example.utils.KafkaConfig

import kafka.producer.{KeyedMessage, ProducerConfig, Producer => KafkaProducer}

import scala.util.Random

import kafka.producer.Partitioner
import kafka.utils.VerifiableProperties


class SimplePartitioner(props: VerifiableProperties) extends Partitioner {
  override def partition(key: Any, numPartitions: Int): Int = {
    Random.nextInt() % numPartitions
  }
}

object SimplePartitioner {
  def apply(props: VerifiableProperties) = new SimplePartitioner(props)
}

case class Producer[A](topic: String) {
  protected val config = new ProducerConfig(KafkaConfig())
  private lazy val producer = new KafkaProducer[String, A](config)
  def send(message: A) = sendMessage(producer, keyedMessage(topic, message))

  def sendStream(stream: Stream[A]) = {
    val iter = stream.iterator
    while(iter.hasNext) {
      send(iter.next())
    }
  }

  private def keyedMessage(topic: String, message: A): KeyedMessage[String, A] = new KeyedMessage[String, A](topic, "ok", message)
  private def sendMessage(producer: KafkaProducer[String, A], message: KeyedMessage[String, A]) = producer.send(message)
}


object Producer {
  def apply[T](topic: String, props: Properties) = new Producer[T](topic) {
    override val config = new ProducerConfig(props)
  }
}