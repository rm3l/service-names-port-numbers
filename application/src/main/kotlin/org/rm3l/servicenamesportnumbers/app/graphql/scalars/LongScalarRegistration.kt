package org.rm3l.servicenamesportnumbers.app.graphql.scalars

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars.GraphQLLong
import graphql.schema.idl.RuntimeWiring

@DgsComponent
class LongScalarRegistration {

  @DgsRuntimeWiring fun addScalar(builder: RuntimeWiring.Builder) = builder.scalar(GraphQLLong)
}
