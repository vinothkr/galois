package galois

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JacksonJsonSupport {

  val jacksonModules = Seq(DefaultScalaModule)

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModules(jacksonModules:_*)

  implicit def marshaller[T]: Marshaller[T, HttpResponse] = {
    Marshaller.withFixedContentType(MediaTypes.`application/json`) {value:T => HttpResponse(entity = HttpEntity(mapper.writeValueAsString(value)))}
  }

}