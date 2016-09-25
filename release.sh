#!/usr/bin/env sh
#
# Copyright © 2016 Schlichtherle IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

SCALA_COMPAT_VERSION=2.12.0-RC1
SCALA_VERSION=$SCALA_COMPAT_VERSION
mvn release:prepare release:perform && \
	cd target/checkout && \
	mvn clean deploy -Prelease \
		-Dscala.compat.version=$SCALA_COMPAT_VERSION \
		-Dscala.version=$SCALA_VERSION \
		-pl :neuron-di-scala_$SCALA_COMPAT_VERSION,:neuron-di-guice-scala_$SCALA_COMPAT_VERSION
