package com.github.music.of.the.ainur.almaren.component.state.core

import org.apache.spark.sql.DataFrame
import com.github.music.of.the.ainur.almaren.component.State
import com.github.music.of.the.ainur.almaren.util.Constants

private[almaren] abstract class Main extends State {
  override def executor(df: DataFrame): DataFrame = core(df)
  def core(df: DataFrame): DataFrame
}

class Sql(sql: String) extends Main {
  override def core(df: DataFrame): DataFrame = sql(df)
  def sql(df: DataFrame): DataFrame = {
    logger.info(s"sql:{$sql}")
    df.createOrReplaceTempView(Constants.TempTableName)
    df.sqlContext.sql(sql)
  }
}

class Coalesce(size:Int) extends Main {
  override def core(df: DataFrame): DataFrame = coalesce(df)
  def coalesce(df: DataFrame): DataFrame = {
    logger.info(s"{$size}")
    df.coalesce(size)
  }
}

class Repartition(size:Int) extends Main {
  override def core(df: DataFrame): DataFrame = repartition(df)
  def repartition(df: DataFrame): DataFrame = {
    logger.info(s"{$size}")
    df.repartition(size)
  }
}

class Pipe(command:String) extends Main {
  override def core(df: DataFrame): DataFrame = pipe(df)
  def pipe(df: DataFrame): DataFrame = {
    import df.sparkSession.implicits._
    logger.info(s"{$command}")
    df.rdd.pipe(command).toDF()
  }
}

class Alias(alias:String) extends Main {
  override def core(df: DataFrame): DataFrame = alias(df)
  def alias(df: DataFrame): DataFrame = {
    logger.info(s"{$alias}")
    df.createOrReplaceTempView(alias)
    df
  }
}

class Cache(opType:Boolean = true,tableName:Option[String] = None) extends Main {
  override def core(df: DataFrame): DataFrame = cache(df)
  def cache(df: DataFrame): DataFrame = {
    logger.info(s"opType:{$opType}, tableName{$tableName}")
    tableName match {
      case Some(t) => cacheTable(df,t)
      case None => cacheDf(df)
    }
    df
  }
  private def cacheDf(df:DataFrame): Unit = opType match {
    case true => df.persist()
    case false => df.unpersist()
  }
  private def cacheTable(df:DataFrame,tableName: String): Unit =
    opType match {
      case true => df.sqlContext.cacheTable(tableName)
      case false => df.sqlContext.uncacheTable(tableName)
    }
}
