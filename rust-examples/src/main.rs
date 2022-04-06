use crate::closures::*;
use crate::conversion::*;
use crate::custom_types::*;
use crate::flow_control::*;
use crate::formatted_print::*;
use crate::functions::*;
use crate::generics::*;
use crate::misc::*;
use crate::modules::*;
use crate::primitives::*;

use crate::test_cynic_starwars::*;
use crate::types::*;

pub mod closures;
pub mod conversion;
pub mod custom_types;
pub mod flow_control;
pub mod formatted_print;
pub mod functions;
pub mod generics;
pub mod misc;
pub mod modules;
pub mod primitives;
pub mod test_cynic;
pub mod test_cynic_starwars;
pub mod traits;
pub mod types;
pub mod variable_bindings;

///为了方便将每个知识点分为单独的源文件，并提供与之相同的公开方法测试内部所有代码
fn main() {
    formatted_print();
    primitives();
    custom_types();
    types();
    conversion();
    flow_control();
    functions();
    closures();
    modules();
    generics();
    traits::traits();
    misc();
    cynic_starwars1();
    // cynic_starwars2();
    cynic_cynic_starwars_schema();
}
