package org.insightedge.geodemo.processing

import com.j_spaces.core.client.SQLQuery
import org.apache.spark.rdd.RDD
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.utils.GridProxyFactory

import scala.reflect._

/**
  * @author Oleksiy_Dyagilev
  */
object RddExtensionImplicit {

  implicit class RddExtension[T: ClassTag](rdd: RDD[T]) extends Serializable {

    val ieConfig = {
      val sparkConf = rdd.sparkContext.getConf
      InsightEdgeConfig.fromSparkConf(sparkConf)
    }

    def flatMapWithGridQuery[U: ClassTag, R: ClassTag](query: String, queryParamsConstructor: T => Seq[Any], f: (T, Seq[U]) =>  Seq[R]): RDD[R] = {
      rdd.mapPartitions { partition =>
        val space = GridProxyFactory.getOrCreateClustered(ieConfig)
        partition.flatMap { item =>
          val clazz = classTag[U].runtimeClass.asInstanceOf[Class[U]]
          val sqlQuery = new SQLQuery[U](clazz, query)
          val queryParams = queryParamsConstructor(item)
          sqlQuery.setParameters(queryParams.map(_.asInstanceOf[Object]): _*)
          val readItems = space.readMultiple(sqlQuery)
          println("readItems: ")
          readItems.foreach(item => println("found =" +  item))
          f(item, readItems)
        }
      }
    }

    def mapWithGridQuery[U: ClassTag, R: ClassTag](query: String, queryParamsConstructor: T => Seq[Any], f: (T, Seq[U]) =>  R): RDD[R] = {
      rdd.mapPartitions { partition =>
        val space = GridProxyFactory.getOrCreateClustered(ieConfig)
        partition.map { item =>
          val clazz = classTag[U].runtimeClass.asInstanceOf[Class[U]]
          val sqlQuery = new SQLQuery[U](clazz, query)
          val queryParams = queryParamsConstructor(item)
          sqlQuery.setParameters(queryParams.map(_.asInstanceOf[Object]): _*)
          val readItems = space.readMultiple(sqlQuery)
          println("readItems: ")
          readItems.foreach(item => println("found =" +  item))
          f(item, readItems)
        }
      }
    }

  }

}
