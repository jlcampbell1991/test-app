package test.app

import doobie._
import doobie.implicits._

object TinyTanManTable extends Table {
  def initialize: Update0 = sql"""
    DROP TABLE IF EXISTS test_app_tiny_tan_man;
    CREATE TABLE test_app_tiny_tan_man(
      name VARCHAR,
      size INTEGER,
      created_at TIMESTAMP,
      updated_at TIMESTAMP,
      id VARCHAR PRIMARY KEY,
      user_id VARCHAR
    )""".update

  def update: Update0 =
    sql"""DROP TABLE IF EXISTS test_app_tiny_tan_man""".update
}
